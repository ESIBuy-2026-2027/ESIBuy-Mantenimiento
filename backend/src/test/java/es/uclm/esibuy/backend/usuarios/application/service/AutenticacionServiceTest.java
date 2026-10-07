package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.when;

// Login, paso 1: credenciales, bloqueo por intentos y cuentas desactivadas (HU2, T19/T22).
// El contrato que debe cumplir sale del frontend (frontend/src/mocks/authHandlers.js).
@ExtendWith(MockitoExtension.class)
class AutenticacionServiceTest {

    @Mock
    private UsuarioRepositoryPort usuarioRepository;

    @Mock
    private HashContrasenaPort hashContrasena;

    private AutenticacionUseCase autenticacion; // Tipado como el puerto de entrada, igual que en el registro

    @BeforeEach
    void crearServicio() {
        autenticacion = new AutenticacionService(usuarioRepository, hashContrasena);
    }

    @Test
    @DisplayName("HU2-CP01: inicia sesión con credenciales correctas y pasa al código por email")
    void iniciaSesionConCredencialesCorrectas() {
        Usuario usuario = new Usuario("juan@uclm.es", "hash-guardado", Rol.CLIENTE);
        when(usuarioRepository.buscarPorCorreo("juan@uclm.es")).thenReturn(Optional.of(usuario));
        when(hashContrasena.coincide("clave1234", "hash-guardado")).thenReturn(true);

        ResultadoInicioSesion resultado = autenticacion.iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));

        assertEquals("juan@uclm.es", resultado.correo());
        assertEquals(PasoLogin.CODIGO_EMAIL, resultado.siguientePaso());
    }

    @Test
    @DisplayName("HU2-CP02: rechaza un correo inexistente")
    void rechazaCorreoInexistente() {
        // TODO: el repositorio no encuentra el correo.
        // TODO: comprobar que lanza CredencialesInvalidasException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU2-CP02: rechaza una contraseña incorrecta con la misma respuesta que un correo inexistente")
    void rechazaContrasenaIncorrecta() {
        // TODO: el usuario existe pero la contraseña no coincide.
        // TODO: comprobar que lanza CredencialesInvalidasException (no se revela que el correo existe).
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU2-CP03: tras 4 intentos fallidos la cuenta no se bloquea")
    void permiteCuartoIntentoFallidoSinBloquear() {
        // Frontera: 4 fallos → aún se puede entrar.
        // TODO: usuario con 4 intentos fallidos.
        // TODO: comprobar que con la contraseña correcta inicia sesión.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU2-CP03: bloquea la cuenta tras 5 intentos fallidos")
    void bloqueaLaCuentaTrasCincoIntentosFallidos() {
        // Frontera: 5 fallos → bloqueada, incluso con la contraseña correcta.
        // TODO: provocar 5 intentos fallidos.
        // TODO: comprobar que la contraseña correcta lanza CuentaBloqueadaException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU2-CP03: desbloquea la cuenta pasados 15 minutos")
    void desbloqueaLaCuentaPasados15Minutos() {
        // TODO: cuenta bloqueada; avanzar el reloj 15 minutos.
        // TODO: comprobar que con la contraseña correcta inicia sesión.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU2-CP04: rechaza una cuenta desactivada")
    void rechazaCuentaDesactivada() {
        // TODO: usuario DESACTIVADO con credenciales correctas.
        // TODO: comprobar que lanza CuentaDesactivadaException.
        fail("Funcionalidad no implementada");
    }
}
