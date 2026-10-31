package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RestablecerContrasenaCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.EnvioEmailPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.GeneradorCodigoPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
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
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Recuperación de contraseña (HU3, T400) con un código de 6 dígitos por correo, como el del login (HU2).
// Paso 1: solicitarla. Siempre termina igual, exista la cuenta o no, para no revelar qué emails están registrados.
// Paso 2: restablecerla con el código y la contraseña nueva. El código vale 10 minutos, es de un solo uso y deja
// de valer tras 5 fallos (reglas de CodigoVerificacion); la contraseña nueva cumple la política de la HU1.
@ExtendWith(MockitoExtension.class)
class RecuperacionContrasenaServiceTest {

    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Juan", "Pérez García", null);
    private static final DatosCliente DATOS_CLIENTE = new DatosCliente(
            TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null);
    private static final String EMAIL = "juan@uclm.es";
    private static final String HASH_ACTUAL = "hash-de-la-contrasena-actual";

    // Hora fija: el servicio la recibe en un Clock para que los tests puedan probar la caducidad.
    private static final Instant AHORA = Instant.parse("2026-10-31T10:00:00Z");

    // Código que "genera" el generador y su hash, para comprobar qué se envía y qué se guarda.
    private static final String CODIGO = "654321";
    private static final String HASH_CODIGO = "hash-del-codigo";

    // Contraseña nueva que cumple la política (y no contiene el nombre, los apellidos ni el email) y su hash.
    private static final String NUEVA = "Nueva-Clave-9!";
    private static final String HASH_NUEVA = "hash-de-la-nueva";

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    @Mock
    private CodigoRecuperacionRepositoryPort codigoRepository;

    @Mock
    private GeneradorCodigoPort generadorCodigo;

    @Mock
    private EnvioEmailPort envioEmail;

    private RecuperacionContrasenaUseCase recuperacion; // Tipado como el puerto de entrada

    @BeforeEach
    void crearServicio() {
        recuperacion = new RecuperacionContrasenaService(usuarioRepository, hashContrasena, codigoRepository,
                generadorCodigo, envioEmail, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static Usuario clientePendiente() {
        return new Usuario("u-1", DATOS_PERSONALES, new Email(EMAIL), HASH_ACTUAL, Rol.CLIENTE, DATOS_CLIENTE, null);
    }

    private static Usuario clienteActivo() {
        Usuario usuario = clientePendiente();
        usuario.activar();
        return usuario;
    }

    // Código de recuperación pendiente: enviado hace un minuto, sin usar y sin fallos.
    private static CodigoVerificacion codigoPendiente() {
        return CodigoVerificacion.reconstruir("c-1", "u-1", HASH_CODIGO, AHORA.plus(Duration.ofMinutes(9)), null, 0);
    }

    private static RestablecerContrasenaCommand comando(String codigo, String password) {
        return new RestablecerContrasenaCommand(EMAIL, codigo, password);
    }

    // Prepara una cuenta activa con un código pendiente; devuelve el código para poder comprobar cómo queda.
    private CodigoVerificacion cuentaConCodigoPendiente(Usuario usuario) {
        CodigoVerificacion codigo = codigoPendiente();
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));
        return codigo;
    }

    // --- Paso 1: solicitar la recuperación ---------------------------------------------------------------------

    @Test
    @DisplayName("HU3-CP02: con una cuenta activa envía un código por correo y guarda solo su hash, válido 10 minutos")
    void solicitarConCuentaActivaEnviaElCodigoYGuardaSoloSuHash() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(clienteActivo()));
        when(generadorCodigo.generar()).thenReturn(CODIGO);
        when(hashContrasena.hashear(CODIGO)).thenReturn(HASH_CODIGO);

        recuperacion.solicitarRecuperacion(EMAIL);

        ArgumentCaptor<CodigoVerificacion> guardado = ArgumentCaptor.forClass(CodigoVerificacion.class);
        verify(codigoRepository).guardar(guardado.capture());
        assertEquals("u-1", guardado.getValue().getUsuarioId());
        assertEquals(HASH_CODIGO, guardado.getValue().getCodigoHash());
        assertEquals(AHORA.plus(Duration.ofMinutes(10)), guardado.getValue().getExpiraEn());
        verify(envioEmail).enviarCodigoRecuperacion(EMAIL, CODIGO);
    }

    @Test
    @DisplayName("HU3-CP02: busca la cuenta con el email normalizado (minúsculas y sin espacios)")
    void solicitarBuscaLaCuentaConElEmailNormalizado() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.empty());

        recuperacion.solicitarRecuperacion("  Juan@UCLM.es ");

        verify(usuarioRepository).buscarPorCorreo(EMAIL);
    }

    @Test
    @DisplayName("HU3-CP03: con un email que no está registrado no envía nada ni lo revela")
    void solicitarConEmailDesconocidoNoEnviaNadaNiLoRevela() {
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.empty());

        assertDoesNotThrow(() -> recuperacion.solicitarRecuperacion(EMAIL));

        verifyNoInteractions(generadorCodigo, codigoRepository, envioEmail);
    }

    @Test
    @DisplayName("HU3-CP03: con un email mal formado no envía nada ni lo revela")
    void solicitarConEmailMalFormadoNoEnviaNadaNiLoRevela() {
        assertDoesNotThrow(() -> recuperacion.solicitarRecuperacion("no-es-un-email"));

        verifyNoInteractions(generadorCodigo, codigoRepository, envioEmail);
    }

    @Test
    @DisplayName("HU3-CP03: a una cuenta pendiente o desactivada no le envía nada, y tampoco lo revela")
    void solicitarConCuentaNoActivaNoEnviaNada() {
        Usuario desactivada = clienteActivo();
        desactivada.desactivar();

        for (Usuario cuenta : List.of(clientePendiente(), desactivada)) {
            when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(cuenta));

            assertDoesNotThrow(() -> recuperacion.solicitarRecuperacion(EMAIL));
        }

        verifyNoInteractions(generadorCodigo, codigoRepository, envioEmail);
    }

    // --- Paso 2: restablecer la contraseña ---------------------------------------------------------------------

    @Test
    @DisplayName("HU3-CP04: con el código correcto guarda el hash de la contraseña nueva y el código queda usado")
    void restablecerConCodigoCorrectoCambiaLaContrasenaYGastaElCodigo() {
        Usuario usuario = clienteActivo();
        CodigoVerificacion codigo = cuentaConCodigoPendiente(usuario);
        when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);
        when(hashContrasena.hashear(NUEVA)).thenReturn(HASH_NUEVA);

        recuperacion.restablecerContrasena(comando(CODIGO, NUEVA));

        assertEquals(HASH_NUEVA, usuario.getPasswordHash());
        verify(usuarioRepository).guardar(usuario);
        assertEquals(AHORA, codigo.getUsadoEn());
        verify(codigoRepository).guardar(codigo);
    }

    @Test
    @DisplayName("HU3-CP05: con un código incorrecto registra el fallo y no cambia la contraseña")
    void restablecerConCodigoIncorrectoRegistraElFalloYNoCambiaNada() {
        Usuario usuario = clienteActivo();
        CodigoVerificacion codigo = cuentaConCodigoPendiente(usuario);
        when(hashContrasena.coincide("000000", HASH_CODIGO)).thenReturn(false);

        assertThrows(CodigoInvalidoException.class, () -> recuperacion.restablecerContrasena(comando("000000", NUEVA)));

        assertEquals(1, codigo.getIntentosFallidos());
        verify(codigoRepository).guardar(codigo);
        assertEquals(HASH_ACTUAL, usuario.getPasswordHash());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU3-CP06: un código caducado, ya usado o con 5 fallos no sirve, aunque sea el correcto")
    void restablecerConCodigoQueYaNoSirveNoCambiaNada() {
        Usuario usuario = clienteActivo();
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(usuario));
        List<CodigoVerificacion> codigosQueNoSirven = List.of(
                CodigoVerificacion.reconstruir("c-1", "u-1", HASH_CODIGO, AHORA, null, 0),
                CodigoVerificacion.reconstruir("c-1", "u-1", HASH_CODIGO, AHORA.plusSeconds(60), AHORA, 0),
                CodigoVerificacion.reconstruir("c-1", "u-1", HASH_CODIGO, AHORA.plusSeconds(60), null, 5));

        for (CodigoVerificacion codigo : codigosQueNoSirven) {
            when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.of(codigo));

            assertThrows(CodigoInvalidoException.class,
                    () -> recuperacion.restablecerContrasena(comando(CODIGO, NUEVA)));
        }

        assertEquals(HASH_ACTUAL, usuario.getPasswordHash());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU3-CP06: sin código pendiente, o con un email desconocido o mal formado, responde como un código incorrecto")
    void restablecerSinCodigoOConEmailDesconocidoRespondeComoCodigoIncorrecto() {
        Usuario usuario = clienteActivo();
        when(usuarioRepository.buscarPorCorreo(EMAIL)).thenReturn(Optional.of(usuario));
        when(codigoRepository.buscarPorUsuario("u-1")).thenReturn(Optional.empty());
        assertThrows(CodigoInvalidoException.class, () -> recuperacion.restablecerContrasena(comando(CODIGO, NUEVA)));

        when(usuarioRepository.buscarPorCorreo("otro@uclm.es")).thenReturn(Optional.empty());
        assertThrows(CodigoInvalidoException.class,
                () -> recuperacion.restablecerContrasena(new RestablecerContrasenaCommand("otro@uclm.es", CODIGO, NUEVA)));

        assertThrows(CodigoInvalidoException.class,
                () -> recuperacion.restablecerContrasena(new RestablecerContrasenaCommand("no-es-un-email", CODIGO, NUEVA)));

        assertEquals(HASH_ACTUAL, usuario.getPasswordHash());
        verify(usuarioRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU3-CP07: si la contraseña nueva no cumple la política, no cambia nada y el código sigue sirviendo")
    void restablecerConContrasenaQueNoCumpleLaPoliticaNoGastaElCodigo() {
        Usuario usuario = clienteActivo();
        CodigoVerificacion codigo = cuentaConCodigoPendiente(usuario);
        when(hashContrasena.coincide(CODIGO, HASH_CODIGO)).thenReturn(true);

        assertThrows(ContrasenaInvalidaException.class, () -> recuperacion.restablecerContrasena(comando(CODIGO, "corta")));

        assertEquals(HASH_ACTUAL, usuario.getPasswordHash());
        assertNull(codigo.getUsadoEn());
        assertEquals(0, codigo.getIntentosFallidos());
        verify(usuarioRepository, never()).guardar(any());
        verify(codigoRepository, never()).guardar(any());

        // El usuario corrige la contraseña y, con el mismo código, ya la cambia.
        clearInvocations(usuarioRepository, codigoRepository);
        when(hashContrasena.hashear(NUEVA)).thenReturn(HASH_NUEVA);
        recuperacion.restablecerContrasena(comando(CODIGO, NUEVA));
        assertEquals(HASH_NUEVA, usuario.getPasswordHash());
    }
}
