package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AltaAuthenticator;
import es.uclm.esibuy.backend.usuarios.application.port.in.AuthenticatorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.CifradoSecretoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AuthenticatorYaDadoDeAltaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Caso de uso del paso 3 del login con Microsoft Authenticator (HU2, T20): alta del secreto con QR y,
// en el ciclo siguiente, verificación del código. Sin Spring: los puertos de salida son mocks.
@ExtendWith(MockitoExtension.class)
class AuthenticatorServiceTest {

    private static final Instant AHORA = Instant.parse("2026-10-11T10:00:00Z");
    private static final String SECRETO = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";
    private static final String SECRETO_CIFRADO = "secreto-cifrado";
    private static final String QR = "data:image/png;base64,qr-de-prueba";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private FactorAutenticacionRepositoryPort factorRepository;

    @Mock
    private TotpPort totp;

    @Mock
    private CifradoSecretoPort cifrado;

    private AuthenticatorUseCase authenticator;

    @BeforeEach
    void crearServicio() {
        authenticator = new AuthenticatorService(usuarioRepository, factorRepository, totp, cifrado,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static Usuario clienteConId() {
        return new Usuario("u-1", new DatosPersonales("Juan", "Pérez García", null), new Email("juan@uclm.es"),
                "hash-guardado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
    }

    @Test
    @DisplayName("HU2-CP22: el alta genera un secreto, lo guarda cifrado y sin confirmar, y devuelve el QR")
    void prepararAltaGuardaElSecretoCifradoYDevuelveElQr() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(totp.generarSecreto()).thenReturn(SECRETO);
        when(cifrado.cifrar(SECRETO)).thenReturn(SECRETO_CIFRADO);
        when(totp.generarQr("juan@uclm.es", SECRETO)).thenReturn(QR);

        AltaAuthenticator alta = authenticator.prepararAlta("juan@uclm.es");

        assertEquals(QR, alta.qr());

        ArgumentCaptor<FactorAutenticacion> guardado = ArgumentCaptor.forClass(FactorAutenticacion.class);
        verify(factorRepository).guardar(guardado.capture());
        assertEquals("u-1", guardado.getValue().getUsuarioId());
        assertFalse(guardado.getValue().estaConfirmado(), "El alta se confirma con el primer código correcto");
    }

    @Test
    @DisplayName("HU2-CP24: el alta guarda el secreto cifrado, nunca en claro")
    void prepararAltaNoGuardaElSecretoEnClaro() {
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(totp.generarSecreto()).thenReturn(SECRETO);
        when(cifrado.cifrar(SECRETO)).thenReturn(SECRETO_CIFRADO);
        when(totp.generarQr("juan@uclm.es", SECRETO)).thenReturn(QR);

        authenticator.prepararAlta("juan@uclm.es");

        ArgumentCaptor<FactorAutenticacion> guardado = ArgumentCaptor.forClass(FactorAutenticacion.class);
        verify(factorRepository).guardar(guardado.capture());
        assertEquals(SECRETO_CIFRADO, guardado.getValue().getSecretoCifrado());
    }

    @Test
    @DisplayName("HU2-CP22: el alta de un correo que no existe se rechaza sin generar nada")
    void prepararAltaDeUnCorreoInexistenteSeRechaza() {
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> authenticator.prepararAlta("nadie@uclm.es"));

        verify(factorRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU2-CP22: quien ya tiene Authenticator confirmado no puede volver a darse de alta")
    void prepararAltaConAuthenticatorConfirmadoSeRechaza() {
        // Si se permitiera, quien conociera la contraseña y el correo de la víctima podría dar de alta
        // SU móvil y saltarse el tercer factor. Cambiar de móvil queda fuera de alcance (D10).
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(clienteConId()));
        when(factorRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(
                FactorAutenticacion.reconstruir("f-1", "u-1", SECRETO_CIFRADO, true, AHORA, null)));

        assertThrows(AuthenticatorYaDadoDeAltaException.class, () -> authenticator.prepararAlta("juan@uclm.es"));

        verify(factorRepository, never()).guardar(any());
        verify(totp, never()).generarSecreto();
    }
}
