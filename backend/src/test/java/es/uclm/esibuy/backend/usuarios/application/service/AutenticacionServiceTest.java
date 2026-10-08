package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Login, paso 1: credenciales, bloqueo por intentos y cuentas desactivadas (HU2, T19/T22).
// El contrato que debe cumplir sale del frontend (frontend/src/mocks/authHandlers.js).
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    // Hora fija para los tests: el usuario recibe la hora en vez de consultarla él mismo.
    private static final Instant AHORA = Instant.parse("2026-10-08T10:00:00Z");

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    private AutenticacionUseCase autenticacion; // Tipado como el puerto de entrada, igual que en el registro

    @BeforeEach
    void crearServicio() {
        autenticacion = new AutenticacionService(usuarioRepository, hashContrasena, Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    @Test
    @DisplayName("HU2-CP01: inicia sesión con credenciales correctas y pasa al código por email")
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals("juan@uclm.es", resultado.email());
        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP02: rechaza un correo inexistente")
    void rechazaCorreoInexistente() {
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());
        IniciarSesionCommand comando = new IniciarSesionCommand("nadie@uclm.es", "clave1234");

        assertThrows(CredencialesInvalidasException.class, () -> autenticacion.iniciarSesion(comando));
    }

    @Test
    @DisplayName("HU2-CP02: rechaza una contraseña incorrecta con la misma respuesta que un correo inexistente")
    void rechazaContrasenaIncorrecta() {
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("otraClave", "hash-guardado")).thenReturn(false);
        when(usuarioRepository.buscarPorCorreo("nadie@uclm.es")).thenReturn(Optional.empty());

        CredencialesInvalidasException contrasenaIncorrecta = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "otraClave")));
        CredencialesInvalidasException correoInexistente = assertThrows(CredencialesInvalidasException.class,
                () -> autenticacion.iniciarSesion(new IniciarSesionCommand("nadie@uclm.es", "otraClave")));

        // Mismo mensaje en los dos casos: desde fuera no se distingue si el correo existe (enumeración de usuarios).
        assertEquals(correoInexistente.getMessage(), contrasenaIncorrecta.getMessage());
    }

    @Test
    @DisplayName("HU2-CP03: tras 4 intentos fallidos la cuenta no se bloquea")
    void permiteCuartoIntentoFallidoSinBloquear() {
        // Frontera: 4 fallos → aún se puede entrar.
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        for (int i = 0; i < 4; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP03: bloquea la cuenta tras 5 intentos fallidos")
    void bloqueaLaCuentaTrasCincoIntentosFallidos() {
        // Frontera: 5 fallos → bloqueada, incluso con la contraseña correcta.
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("otraClave", "hash-guardado")).thenReturn(false);
        IniciarSesionCommand contrasenaIncorrecta = new IniciarSesionCommand("juan@uclm.es", "otraClave");
        for (int i = 0; i < 5; i++) {
            assertThrows(CredencialesInvalidasException.class, () -> autenticacion.iniciarSesion(contrasenaIncorrecta));
        }

        // La contraseña correcta no se llega a comprobar: si se respondiera distinto al acertar,
        // un atacante sabría cuándo ha dado con ella.
        IniciarSesionCommand contrasenaCorrecta = new IniciarSesionCommand("juan@uclm.es", "clave1234");
        assertThrows(CuentaBloqueadaException.class, () -> autenticacion.iniciarSesion(contrasenaCorrecta));

        // Cada fallo se guarda; si no, el contador se perdería entre un login y otro.
        verify(usuarioRepository, times(5)).guardar(usuario);
    }

    @Test
    @DisplayName("HU2-CP03: desbloquea la cuenta pasados 15 minutos")
    void desbloqueaLaCuentaPasados15Minutos() {
        // Frontera: justo a los 15 minutos del bloqueo ya se puede entrar.
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        for (int i = 0; i < 5; i++) {
            usuario.registrarIntentoFallido(AHORA);
        }
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        // El mismo servicio, pero con el reloj 15 minutos después: no hace falta esperar.
        Clock relojQuinceMinutosDespues = Clock.fixed(AHORA.plus(Duration.ofMinutes(15)), ZoneOffset.UTC);
        AutenticacionUseCase autenticacionMasTarde = new AutenticacionService(usuarioRepository, hashContrasena, relojQuinceMinutosDespues);

        ResultadoInicioSesion resultado = autenticacionMasTarde.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP04: rechaza una cuenta desactivada")
    void rechazaCuentaDesactivada() {
        // «Desactivada» = desactivada por un administrador (desactivar() de Usuario, T214).
        // Se comprueba después de la contraseña: quien no la conoce no puede averiguar el estado de la cuenta.
        Usuario usuario = Usuario.nuevoCliente(new Email("juan@uclm.es"), "hash-guardado");
        usuario.desactivar();
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);
        IniciarSesionCommand comando = new IniciarSesionCommand("juan@uclm.es", "clave1234");

        assertThrows(CuentaDesactivadaException.class, () -> autenticacion.iniciarSesion(comando));
    }
}
