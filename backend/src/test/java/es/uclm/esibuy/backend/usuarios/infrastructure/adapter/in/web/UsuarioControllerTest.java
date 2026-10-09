package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
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

    @MockitoBean
    private ListadoUsuariosUseCase listadoUsuarios;

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
    void correoYaRegistradoDevuelve409() throws Exception {
        // registrar devuelve void: por eso doThrow(...).when(...) y no when(...).thenThrow(...)
        doThrow(new CorreoYaRegistradoException("El correo ya está registrado"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje").value(not(containsString("juan@uclm.es"))));
    }

    @Test
    @DisplayName("HU1-CP05: una contraseña que no cumple la política devuelve 400")
    void contrasenaInvalidaDevuelve400() throws Exception {
        doThrow(new ContrasenaInvalidaException("La contraseña debe tener mínimo 8 caracteres"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "1234567")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU1-CP07: un email sin dominio devuelve 400 con un mensaje genérico")
    void emailSinDominioDevuelve400ConMensajeGenerico() throws Exception {
        // La validación web (@Email) deja pasar "ana@localhost", pero el dominio lo rechaza.
        // El doble lanza la excepción con el mensaje real del dominio, que incluye el email.
        doThrow(new EmailInvalidoException("El email no tiene un formato válido: ana@localhost"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("ana@localhost", "clave1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje").value(not(containsString("ana@localhost"))));
    }

    @Test
    @DisplayName("Seguridad: una ruta distinta del registro exige sesión")
    void otraRutaSinSesionEstaProtegida() throws Exception {
        // 403 y no 401: todavía no hay ningún mecanismo de login configurado (llegará con la HU2)
        mockMvc.perform(post("/api/auth/otra-ruta")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isForbidden());

        verify(registroUsuario, never()).registrar(any());
    }

    @Test
    @DisplayName("Seguridad: el listado administrativo sin sesión devuelve 403")
    void listadoSinSesionEstaProtegido() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/admin/usuarios"))
                .andExpect(status().isForbidden());

        verify(listadoUsuarios, never()).listar(any());
    }

    @Test
    @DisplayName("HU19-CP08: un administrador recibe el listado sin datos sensibles")
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMINISTRADOR")
    void administradorRecibeElListado() throws Exception {
        Usuario usuario = new Usuario(
                "usr-1", "Ana", "García", new Email("ana@esibuy.test"),
                "hash-no-debe-salir", Rol.CLIENTE, TipoCliente.ESTANDAR,
                java.time.Instant.parse("2026-10-01T10:00:00Z")
        );
        when(listadoUsuarios.listar(any())).thenReturn(new UsuarioPage(
                java.util.List.of(usuario), 0, 10, 1
        ));

        mockMvc.perform(get("/api/admin/usuarios")
                        .param("page", "1")
                        .param("size", "10")
                        .param("search", "ana")
                        .param("role", "CLIENTE")
                        .param("status", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("usr-1"))
                .andExpect(jsonPath("$.items[0].email").value("ana@esibuy.test"))
                .andExpect(jsonPath("$.items[0].estado").value("ACTIVO"))
                .andExpect(jsonPath("$.items[0].fechaAlta").value("2026-10-01T10:00:00Z"))
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        verify(listadoUsuarios).listar(new ListarUsuariosQuery(
                "ana", Rol.CLIENTE, "ACTIVO", 0, 10
        ));
    }
}
