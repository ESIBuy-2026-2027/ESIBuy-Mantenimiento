package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

// Tests del adaptador web del registro (HU1, AB#14).
// Se prueban también SecurityConfig (shared.config) y ManejadorGlobalErrores (shared.error)
// a través del endpoint, sin levantar MongoDB: el caso de uso se sustituye por un doble.
// PENDIENTE DE REVISAR CON EL EQUIPO: los IDs HU1-CP07 y HU1-CP08 son una propuesta.
class UsuarioControllerTest {

    @Test
    @DisplayName("HU1-CP01: un registro válido devuelve 201 y delega en el caso de uso")
    void registroValidoDevuelve201() {
        // TODO POST /api/auth/registro con email y password válidos -> 201
        //      y el caso de uso recibe RegistrarUsuarioCommand("juan@uclm.es", "clave1234")
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP07: un email con formato inválido devuelve 400 y no registra")
    void rechazaEmailConFormatoInvalido() {
        // TODO email "juan.uclm.es" -> 400 y el caso de uso no se llama
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP08: un email vacío devuelve 400 y no registra")
    void rechazaEmailVacio() {
        // TODO email "" -> 400 y el caso de uso no se llama
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP08: una contraseña vacía devuelve 400 y no registra")
    void rechazaPasswordVacia() {
        // TODO password "" -> 400 y el caso de uso no se llama
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP03: un correo ya registrado devuelve 409 con mensaje genérico")
    void correoYaRegistradoDevuelve409() {
        // TODO el caso de uso lanza CorreoYaRegistradoException -> 409 con {"mensaje": ...}
        //      que no contiene el correo
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP05: una contraseña que no cumple la política devuelve 400")
    void contrasenaInvalidaDevuelve400() {
        // TODO el caso de uso lanza ContrasenaInvalidaException -> 400 con {"mensaje": ...}
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("Seguridad: una ruta distinta del registro exige sesión")
    void otraRutaSinSesionEstaProtegida() {
        // TODO POST /api/auth/otra-ruta sin sesión -> 403 y el caso de uso no se llama
        fail("Funcionalidad no implementada");
    }
}
