package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.security.test.context.support.WithMockUser;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests del adaptador web del registro (HU1, AB#14).
// Se prueban también SecurityConfig (shared.config) y ManejadorGlobalErrores (shared.error)
// a través del endpoint, sin levantar MongoDB: el caso de uso se sustituye por un doble.
// PENDIENTE DE REVISAR CON EL EQUIPO: los IDs HU1-CP07 y HU1-CP08 son una propuesta.
@WebMvcTest(UsuarioController.class)
@Import(SecurityConfig.class)
class UsuarioControllerTest {

    private static final String URL_REGISTRO = "/api/auth/register/cliente";   // contrato acordado con el frontend (T229)

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroUsuarioUseCase registroUsuario;

    @MockitoBean
    private ListadoUsuariosUseCase listadoUsuarios;

    @MockitoBean
    private AdministracionUsuariosUseCase administracionUsuarios;

    // Cuerpo completo del registro de cliente (T229); solo varían el email y la contraseña.
    private static String json(String email, String password) {
        return """
               {"nombre": "Juan", "apellidos": "Pérez García", "fechaNacimiento": "1996-05-20",
                "dni": "12345678Z", "email": "%s", "telefono": null, "password": "%s",
                "repeticionPassword": "%s", "tipoCliente": "ESTANDAR", "imagenPerfil": null,
                "aceptaPrivacidad": true}
               """.formatted(email, password, password);
    }

    @Test
    @DisplayName("HU1-CP01: un registro válido devuelve 201 y delega en el caso de uso")
    void registroValidoDevuelve201() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isCreated());

        verify(registroUsuario).registrar(new RegistrarUsuarioCommand("Juan", "Pérez García",
                LocalDate.of(1996, 5, 20), "12345678Z", "juan@uclm.es", null, "clave1234", "clave1234",
                TipoCliente.ESTANDAR, null, true));
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
    @DisplayName("HU1-CP03: un correo ya registrado devuelve 409 con su código y mensaje genérico")
    void correoYaRegistradoDevuelve409() throws Exception {
        // registrar devuelve void: por eso doThrow(...).when(...) y no when(...).thenThrow(...)
        doThrow(new CorreoYaRegistradoException("El correo ya está registrado"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"))   // el frontend marca el campo por el código
                .andExpect(jsonPath("$.mensaje").exists())
                .andExpect(jsonPath("$.mensaje").value(not(containsString("juan@uclm.es"))));
    }

    @Test
    @DisplayName("HU1-CP16: un DNI ya registrado devuelve 409 con su código y mensaje genérico")
    void dniYaRegistradoDevuelve409ConCodigo() throws Exception {
        doThrow(new DniYaRegistradoException("El DNI ya está registrado"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DNI_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("12345678Z"))));
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
    @DisplayName("HU1-CP08: un nombre de 31 caracteres devuelve 400 y no registra")
    void nombreDe31CaracteresDevuelve400() throws Exception {
        // Frontera del máximo del Product Owner (30): la capa web la comprueba antes de llegar al caso de uso
        String cuerpo = json("juan@uclm.es", "clave1234").replace("\"nombre\": \"Juan\"", "\"nombre\": \"" + "J".repeat(31) + "\"");

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrar(any());
    }

    @Test
    @DisplayName("HU1-CP08: un email de 101 caracteres devuelve 400 y no registra")
    void emailDe101CaracteresDevuelve400() throws Exception {
        // Parte local de 61 y dominio de 39: cada trozo respeta los límites de @Email, así que lo único
        // que puede rechazar los 101 caracteres es el máximo de 100 del contrato (frontera: 100 se acepta).
        String dominio = "@" + "b".repeat(36) + ".es";
        String de100 = "a".repeat(100 - dominio.length()) + dominio;
        String de101 = "a".repeat(101 - dominio.length()) + dominio;

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json(de100, "clave1234")))
                .andExpect(status().isCreated());
        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json(de101, "clave1234")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, times(1)).registrar(any());
    }

    @Test
    @DisplayName("HU1-CP15: unos datos de cliente inválidos devuelven 400 con un mensaje genérico")
    void datosDelClienteInvalidosDevuelven400() throws Exception {
        // El dominio rechaza, por ejemplo, un DNI mal formado o un menor de edad con UsuarioInvalidoException:
        // es un error del cliente y debe ser un 400, no un 500. El mensaje no repite el dato.
        doThrow(new UsuarioInvalidoException("El DNI no tiene un formato válido"))
                .when(registroUsuario).registrar(any());

        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").value("Datos de registro no válidos"));
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
    @DisplayName("Seguridad: el listado administrativo sin sesión devuelve 401")
    void listadoSinSesionEstaProtegido() throws Exception {
        // 401 sin sesión y 403 con sesión sin permiso (T230, D10); antes, el 403 por defecto de Spring
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                        .get("/api/admin/usuarios"))
                .andExpect(status().isUnauthorized());

        verify(listadoUsuarios, never()).listar(any());
    }

    @Test
    @DisplayName("HU19-CP08: un administrador recibe el listado sin datos sensibles")
    @org.springframework.security.test.context.support.WithMockUser(roles = "ADMINISTRADOR")
    void administradorRecibeElListado() throws Exception {
        Usuario usuario = new Usuario(
                "usr-1", new DatosPersonales("Ana", "García", null), new Email("ana@esibuy.test"),
                "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null
        );
        usuario.restaurarFechaAlta(java.time.Instant.parse("2026-10-01T10:00:00Z"));
        usuario.activar();   // una cuenta nueva nace PENDIENTE (T233): el test lista una cuenta ya verificada
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

    @Test
    @DisplayName("HU24-CP01: un administrador activa una cuenta y recibe sus datos con estado ACTIVO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorActivaUnaCuentaYRecibeSusDatos() throws Exception {
        Usuario activado = new Usuario("usr-1", new DatosPersonales("Lucía", "Torres Vega", null),
                new Email("lucia@esibuy.test"), "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        activado.activar();   // el caso de uso devuelve la cuenta ya activada (una nueva nace PENDIENTE, T233)
        when(administracionUsuarios.activar("usr-1")).thenReturn(activado);

        mockMvc.perform(cambiarEstado("usr-1", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-1"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(administracionUsuarios).activar("usr-1");
    }

    @Test
    @DisplayName("HU24-CP02: rechaza un estado distinto de ACTIVO y no cambia nada")
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaUnEstadoDistintoDeActivo() throws Exception {
        // DESACTIVADO lo añadirá la HU23 con sus reglas.
        mockMvc.perform(cambiarEstado("usr-1", "DESACTIVADO"))
                .andExpect(status().isBadRequest());

        verify(administracionUsuarios, never()).activar(any());
    }

    @Test
    @DisplayName("HU24-CP03: activar un usuario que no existe devuelve 404")
    @WithMockUser(roles = "ADMINISTRADOR")
    void activarUnUsuarioInexistenteDevuelve404() throws Exception {
        when(administracionUsuarios.activar("no-existe"))
                .thenThrow(new UsuarioNoEncontradoException("El usuario no existe"));

        mockMvc.perform(cambiarEstado("no-existe", "ACTIVO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU24-CP04: un usuario que no es administrador no puede activar cuentas")
    void unNoAdministradorNoPuedeActivarCuentas(String rol) throws Exception {
        mockMvc.perform(cambiarEstado("usr-1", "ACTIVO").with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).activar(any());
    }

    // Acceso por rol a los endpoints reales de administración (HU18, T60). La regla la puso la T58
    // en SecurityConfig; aquí se comprueba sobre el listado (HU19) y la activación (HU24).

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU18-CP02: un cliente o un vendedor no accede al listado de usuarios de administración")
    void noAdministradorNoAccedeAlListado(String rol) throws Exception {
        mockMvc.perform(get("/api/admin/usuarios").with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(listadoUsuarios, never()).listar(any());
    }

    @Test
    @DisplayName("HU18-CP01: sin sesión no se puede activar una cuenta")
    void activarSinSesionEstaProtegido() throws Exception {
        // 401 sin sesión y 403 con sesión sin permiso (T230, D10)
        mockMvc.perform(cambiarEstado("usr-1", "ACTIVO"))
                .andExpect(status().isUnauthorized());

        verify(administracionUsuarios, never()).activar(any());
    }

    // PATCH con sesión: lleva el token CSRF (la estrategia de CSRF se decidirá con el login, task 230).
    private static MockHttpServletRequestBuilder cambiarEstado(String id, String estado) {
        return patch("/api/admin/usuarios/{id}/status", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"" + estado + "\"}");
    }
}
