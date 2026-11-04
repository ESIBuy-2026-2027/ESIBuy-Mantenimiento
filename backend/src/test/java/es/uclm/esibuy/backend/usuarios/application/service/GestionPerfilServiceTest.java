package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Consulta del perfil propio (HU4, T297) y eliminación de la cuenta propia del cliente (HU25, T275). El
// repositorio y el hash son dobles de prueba: se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU4-CP01 y CP04, y HU25-CP01 y CP02, propuestos.
@ExtendWith(MockitoExtension.class)
class GestionPerfilServiceTest {

    private static final String EMAIL = "lucia@esibuy.test";

    private static final String PASSWORD = "Clave-Segura-123";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    @InjectMocks
    private GestionPerfilService servicio;

    @Test
    @DisplayName("HU4-CP01: consultar el perfil devuelve el usuario del correo de la sesión")
    void consultarPerfilDevuelveElUsuarioDelCorreo() {
        Usuario lucia = lucia();
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia));

        assertSame(lucia, servicio.consultarPerfil(EMAIL));
    }

    @Test
    @DisplayName("HU4-CP04: si el usuario de la sesión ya no existe, la sesión no es válida")
    void consultarPerfilDeUnCorreoQueNoExisteLanzaExcepcion() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.empty());

        assertThrows(CredencialesInvalidasException.class, () -> servicio.consultarPerfil(EMAIL));
    }

    @Test
    @DisplayName("HU25-CP01: con la contraseña actual correcta se elimina la cuenta del cliente de la sesión")
    void eliminarCuentaConLaContrasenaCorrectaBorraAlUsuario() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        when(hashContrasena.coincide(PASSWORD, "hash-simulado")).thenReturn(true);

        servicio.eliminarCuenta(EMAIL, PASSWORD);

        verify(usuarioRepository).eliminarPorId("u-1");
    }

    @Test
    @DisplayName("HU25-CP02: con una contraseña incorrecta no se elimina la cuenta")
    void eliminarCuentaConLaContrasenaIncorrectaNoBorraNada() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(lucia()));
        when(hashContrasena.coincide("Otra-Clave-1", "hash-simulado")).thenReturn(false);

        assertThrows(ContrasenaActualIncorrectaException.class, () -> servicio.eliminarCuenta(EMAIL, "Otra-Clave-1"));
        verify(usuarioRepository, never()).eliminarPorId(anyString());
    }

    private static Usuario lucia() {
        return new Usuario("u-1", new DatosPersonales("Lucía", "Torres Vega", null), new Email(EMAIL),
                "hash-simulado", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, Month.MAY, 20), null),
                null);
    }
}
