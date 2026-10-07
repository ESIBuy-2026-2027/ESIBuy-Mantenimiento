package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests del adaptador web del registro (HU1, AB#14).
// Se prueban también SecurityConfig (shared.config) y ManejadorGlobalErrores (shared.error)
// a través del endpoint, sin levantar MongoDB: el caso de uso se sustituye por un doble.
// PENDIENTE DE REVISAR CON EL EQUIPO: los IDs HU1-CP07 y HU1-CP08 son una propuesta.
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    private static final String URL_REGISTRO = "/api/auth/registro";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroUsuarioUseCase registroUsuario;

    private static String json(String email, String password) {
        return """
               {"email": "%s", "password": "%s"}
               """.formatted(email, password);
    }

    @Test
    @DisplayName("HU1-CP01: un registro válido devuelve 201 y delega en el caso de uso")
    void registroValidoDevuelve201() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isCreated());

        verify(registroUsuario).registrar(new RegistrarUsuarioCommand("juan@uclm.es", "clave1234"));
    }

    @Test
    @DisplayName("HU1-CP07: un email con formato inválido devuelve 400 y no registra")
    void rechazaEmailConFormatoInvalido() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan.uclm.es", "clave1234")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrar(any());
    }

    @Test
    @DisplayName("HU1-CP08: un email vacío devuelve 400 y no registra")
    void rechazaEmailVacio() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "clave1234")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrar(any());
    }

    @Test
    @DisplayName("HU1-CP08: una contraseña vacía devuelve 400 y no registra")
    void rechazaPasswordVacia() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrar(any());
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
