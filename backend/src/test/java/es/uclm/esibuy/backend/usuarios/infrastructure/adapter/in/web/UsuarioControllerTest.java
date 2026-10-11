package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarClienteCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarVendedorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYNombreComercialYaRegistradosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
    private static final String URL_REGISTRO_VENDEDOR = "/api/auth/register/vendedor";   // contrato del frontend (T241)

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegistroUsuarioUseCase registroUsuario;

    @MockitoBean
    private ListadoUsuariosUseCase listadoUsuarios;

    @MockitoBean
    private AdministracionUsuariosUseCase administracionUsuarios;

    @MockitoBean
    private CrearAdministradorUseCase crearAdministrador;

    // El nombre de la categoría principal del vendedor sale del módulo de categorías (su puerto de entrada).
    @MockitoBean
    private ConsultaCategoriasUseCase consultaCategorias;

    // Cuerpo completo del registro de cliente (T229); solo varían el email y la contraseña.
    private static String json(String email, String password) {
        return """
               {"nombre": "Juan", "apellidos": "Pérez García", "fechaNacimiento": "1996-05-20",
                "dni": "12345678Z", "email": "%s", "telefono": null, "password": "%s",
                "repeticionPassword": "%s", "tipoCliente": "ESTANDAR", "avatar": null,
                "aceptaPrivacidad": true}
               """.formatted(email, password, password);
    }

    // Cuerpo del formulario de vendedor tras el PR #81: una única categoría en categoriaPrincipalId.
    private static String jsonVendedor() {
        return """
               {"nombre": "Luis", "apellidos": "Pérez García", "nombreComercial": "Tienda Luis",
                "categoriaPrincipalId": "cat-1", "dni": "87654321X", "email": "luis@uclm.es",
                "telefono": "612345678", "password": "Clave-1234", "repeticionPassword": "Clave-1234",
                "avatar": "avatar1", "aceptaPrivacidad": true}
               """;
    }

    @Test
    @DisplayName("HU1-CP01: un registro válido devuelve 201 y delega en el caso de uso")
    void registroValidoDevuelve201() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "clave1234")))
                .andExpect(status().isCreated());

        verify(registroUsuario).registrarCliente(new RegistrarClienteCommand("Juan", "Pérez García",
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

        verify(registroUsuario, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("HU1-CP08: un email vacío devuelve 400 y no registra")
    void rechazaEmailVacio() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("", "clave1234")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("HU1-CP08: una contraseña vacía devuelve 400 y no registra")
    void rechazaPasswordVacia() throws Exception {
        mockMvc.perform(post(URL_REGISTRO)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json("juan@uclm.es", "")))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrarCliente(any());
    }

    @Test
    @DisplayName("HU1-CP03: un correo ya registrado devuelve 409 con su código y mensaje genérico")
    void correoYaRegistradoDevuelve409() throws Exception {
        // registrarCliente devuelve void: por eso doThrow(...).when(...) y no when(...).thenThrow(...)
        doThrow(new CorreoYaRegistradoException("El correo ya está registrado"))
                .when(registroUsuario).registrarCliente(any());

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
                .when(registroUsuario).registrarCliente(any());

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
                .when(registroUsuario).registrarCliente(any());

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
                .when(registroUsuario).registrarCliente(any());

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

        verify(registroUsuario, never()).registrarCliente(any());
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

        verify(registroUsuario, times(1)).registrarCliente(any());
    }

    @Test
    @DisplayName("HU1-CP15: unos datos de cliente inválidos devuelven 400 con un mensaje genérico")
    void datosDelClienteInvalidosDevuelven400() throws Exception {
        // El dominio rechaza, por ejemplo, un DNI mal formado o un menor de edad con UsuarioInvalidoException:
        // es un error del cliente y debe ser un 400, no un 500. El mensaje no repite el dato.
        doThrow(new UsuarioInvalidoException("El DNI no tiene un formato válido"))
                .when(registroUsuario).registrarCliente(any());

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

        verify(registroUsuario, never()).registrarCliente(any());
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
    @DisplayName("HU24-CP02: rechaza un estado que no es ACTIVO ni DESACTIVADO y no cambia nada")
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaUnEstadoNoAdmitido() throws Exception {
        // Desde la HU23 se admiten ACTIVO y DESACTIVADO; cualquier otro estado es un 400.
        mockMvc.perform(cambiarEstado("usr-1", "BLOQUEADO"))
                .andExpect(status().isBadRequest());

        verify(administracionUsuarios, never()).activar(any());
        verify(administracionUsuarios, never()).desactivar(any(), any());
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

    // Desactivar una cuenta (HU23, T37/T38/T39): mismo endpoint que activar, con {"estado": "DESACTIVADO"}.
    // El usuario en sesión se identifica por su email (como en /api/auth/me): con @WithMockUser es el username.
    // Los códigos de los 409 son los que espera el frontend (frontend/docs/hu23-desactivar-usuario.md).
    private static final String EMAIL_ADMINISTRADOR = "admin@esibuy.test";

    @Test
    @DisplayName("HU23-CP01: un administrador desactiva una cuenta y recibe sus datos con estado DESACTIVADO")
    @WithMockUser(username = EMAIL_ADMINISTRADOR, roles = "ADMINISTRADOR")
    void administradorDesactivaUnaCuentaYRecibeSusDatos() throws Exception {
        Usuario desactivado = new Usuario("usr-1", new DatosPersonales("Carlos", "Moreno Gil", null),
                new Email("carlos@esibuy.test"), "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1990, Month.MARCH, 14), null),
                null);
        desactivado.desactivar();
        when(administracionUsuarios.desactivar("usr-1", EMAIL_ADMINISTRADOR)).thenReturn(desactivado);

        mockMvc.perform(cambiarEstado("usr-1", "DESACTIVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-1"))
                .andExpect(jsonPath("$.estado").value("DESACTIVADO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(administracionUsuarios).desactivar("usr-1", EMAIL_ADMINISTRADOR);
        verify(administracionUsuarios, never()).activar(any());
    }

    @Test
    @DisplayName("HU23-CP02: desactivar un usuario que no existe devuelve 404")
    @WithMockUser(username = EMAIL_ADMINISTRADOR, roles = "ADMINISTRADOR")
    void desactivarUnUsuarioInexistenteDevuelve404() throws Exception {
        when(administracionUsuarios.desactivar("no-existe", EMAIL_ADMINISTRADOR))
                .thenThrow(new UsuarioNoEncontradoException("El usuario no existe"));

        mockMvc.perform(cambiarEstado("no-existe", "DESACTIVADO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU23-CP03: si el administrador intenta desactivarse a sí mismo devuelve 409 CANNOT_DEACTIVATE_SELF")
    @WithMockUser(username = EMAIL_ADMINISTRADOR, roles = "ADMINISTRADOR")
    void desactivarseASiMismoDevuelve409() throws Exception {
        when(administracionUsuarios.desactivar("u-1", EMAIL_ADMINISTRADOR))
                .thenThrow(new AutodesactivacionNoPermitidaException("Un administrador no puede desactivar su propia cuenta"));

        mockMvc.perform(cambiarEstado("u-1", "DESACTIVADO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CANNOT_DEACTIVATE_SELF"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU23-CP04: desactivar al último administrador activo devuelve 409 LAST_ACTIVE_ADMIN")
    @WithMockUser(username = EMAIL_ADMINISTRADOR, roles = "ADMINISTRADOR")
    void desactivarAlUltimoAdministradorActivoDevuelve409() throws Exception {
        when(administracionUsuarios.desactivar("u-3", EMAIL_ADMINISTRADOR))
                .thenThrow(new UltimoAdministradorActivoException("No se puede desactivar al último administrador activo"));

        mockMvc.perform(cambiarEstado("u-3", "DESACTIVADO"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("LAST_ACTIVE_ADMIN"))
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU23-CP06: un usuario que no es administrador no puede desactivar cuentas")
    void unNoAdministradorNoPuedeDesactivarCuentas(String rol) throws Exception {
        mockMvc.perform(cambiarEstado("usr-1", "DESACTIVADO").with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).desactivar(any(), any());
    }

    @Test
    @DisplayName("HU23-CP06: sin sesión no se puede desactivar una cuenta")
    void desactivarSinSesionEstaProtegido() throws Exception {
        mockMvc.perform(cambiarEstado("usr-1", "DESACTIVADO"))
                .andExpect(status().isUnauthorized());

        verify(administracionUsuarios, never()).desactivar(any(), any());
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

    // Ficha de un usuario (HU20, T54): GET /api/admin/usuarios/{id} con UsuarioDetalleResponse.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU20-CP01 a CP04, propuestos a partir del contrato.

    @Test
    @DisplayName("HU20-CP01: la ficha de un cliente trae sus datos personales y de cliente, sin datos sensibles")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnClienteConSusDatos() throws Exception {
        Usuario cliente = new Usuario("usr-4", new DatosPersonales("Lucía", "Torres Vega", "avatar2"),
                new Email("lucia@esibuy.test"), "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.PREMIUM, new Dni("12345678Z"), LocalDate.of(1979, 5, 14),
                        new Telefono("620031676")), null);
        when(administracionUsuarios.consultar("usr-4")).thenReturn(cliente);

        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-4"))
                .andExpect(jsonPath("$.nombre").value("Lucía"))
                .andExpect(jsonPath("$.email").value("lucia@esibuy.test"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.dni").value("12345678Z"))
                .andExpect(jsonPath("$.telefono").value("620031676"))
                .andExpect(jsonPath("$.fechaNacimiento").value("1979-05-14"))
                .andExpect(jsonPath("$.tipoCliente").value("PREMIUM"))
                .andExpect(jsonPath("$.avatar").value("avatar2"))
                .andExpect(jsonPath("$.imagenPerfil").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("HU20-CP02: la ficha de un administrador no trae datos de cliente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnAdministradorSinDatosDeCliente() throws Exception {
        Usuario administrador = new Usuario("usr-9", new DatosPersonales("Ana", "López Díaz", null),
                new Email("ana@esibuy.test"), "hash-no-debe-salir", Rol.ADMINISTRADOR, null, null);
        when(administracionUsuarios.consultar("usr-9")).thenReturn(administrador);

        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("ADMINISTRADOR"))
                .andExpect(jsonPath("$.dni").doesNotExist())
                .andExpect(jsonPath("$.telefono").doesNotExist())
                .andExpect(jsonPath("$.fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$.tipoCliente").doesNotExist());
    }

    // Contrato del frontend: la fecha de incorporación del administrador es su fecha de alta (automática y no
    // editable). Como el rol no cambia nunca, la fecha de alta de un administrador es la de su incorporación.
    @Test
    @DisplayName("HU20-CP05: la ficha de un administrador trae su fecha de incorporación, que es su fecha de alta")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnAdministradorConSuFechaDeIncorporacion() throws Exception {
        Usuario administrador = new Usuario("usr-9", new DatosPersonales("Ana", "López Díaz", null),
                new Email("ana@esibuy.test"), "hash-no-debe-salir", Rol.ADMINISTRADOR, null, null);
        administrador.restaurarFechaAlta(java.time.Instant.parse("2026-10-01T10:00:00Z"));
        when(administracionUsuarios.consultar("usr-9")).thenReturn(administrador);

        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-9"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaAlta").value("2026-10-01T10:00:00Z"))
                .andExpect(jsonPath("$.fechaIncorporacion").value("2026-10-01T10:00:00Z"));
    }

    @Test
    @DisplayName("HU20-CP05: la ficha de un cliente no trae fecha de incorporación")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnClienteSinFechaDeIncorporacion() throws Exception {
        Usuario cliente = clienteCarla();
        cliente.restaurarFechaAlta(java.time.Instant.parse("2026-10-01T10:00:00Z"));
        when(administracionUsuarios.consultar("usr-1")).thenReturn(cliente);

        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fechaAlta").value("2026-10-01T10:00:00Z"))
                .andExpect(jsonPath("$.fechaIncorporacion").doesNotExist());
    }

    @Test
    @DisplayName("HU20-CP06: la ficha de un vendedor trae sus datos de vendedor y no los de cliente")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnVendedorConSusDatosDeVendedor() throws Exception {
        when(administracionUsuarios.consultar("v-1")).thenReturn(vendedor());

        mockMvc.perform(get("/api/admin/usuarios/{id}", "v-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.rol").value("VENDEDOR"))
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Marta"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("cat-1"))
                .andExpect(jsonPath("$.dni").value("87654321X"))
                .andExpect(jsonPath("$.telefono").value("612345678"))
                .andExpect(jsonPath("$.fechaNacimiento").doesNotExist())
                .andExpect(jsonPath("$.tipoCliente").doesNotExist());
    }

    @Test
    @DisplayName("HU20-CP03: la ficha de un usuario que no existe devuelve 404")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnUsuarioInexistenteDevuelve404() throws Exception {
        when(administracionUsuarios.consultar("no-existe"))
                .thenThrow(new UsuarioNoEncontradoException("El usuario no existe"));

        mockMvc.perform(get("/api/admin/usuarios/{id}", "no-existe"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU20-CP04: sin sesión no se ve la ficha de un usuario")
    void fichaSinSesionDevuelve401() throws Exception {
        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-4"))
                .andExpect(status().isUnauthorized());

        verify(administracionUsuarios, never()).consultar(any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU20-CP04: un usuario que no es administrador no ve la ficha de otro")
    void noAdministradorNoVeLaFicha(String rol) throws Exception {
        mockMvc.perform(get("/api/admin/usuarios/{id}", "usr-4").with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).consultar(any());
    }

    // Supervisión de vendedores (HU26, T30): GET /api/admin/sellers y /api/admin/sellers/{id}.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU26-CP01 a CP05, propuestos a partir de los
    // criterios de aceptación y del contrato.

    private static Usuario vendedor() {
        return new Usuario("v-1", new DatosPersonales("Marta", "Ruiz Gil", null),
                new Email("marta@esibuy.test"), "hash-no-debe-salir", Rol.VENDEDOR,
                new DatosVendedor("Tienda Marta", "cat-1", new Dni("87654321X"), new Telefono("612345678")), null);
    }

    @Test
    @DisplayName("HU26-CP01: un administrador recibe el listado de vendedores sin datos sensibles")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorRecibeElListadoDeVendedores() throws Exception {
        when(listadoUsuarios.listar(any())).thenReturn(new UsuarioPage(java.util.List.of(vendedor()), 1, 10, 11));
        when(consultaCategorias.listar()).thenReturn(java.util.List.of(new Categoria("cat-1", "Tecnología")));

        mockMvc.perform(get("/api/admin/sellers")
                        .param("page", "2")
                        .param("size", "10")
                        .param("search", "marta")
                        .param("status", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].id").value("v-1"))
                .andExpect(jsonPath("$.items[0].nombre").value("Marta"))
                .andExpect(jsonPath("$.items[0].apellidos").value("Ruiz Gil"))
                .andExpect(jsonPath("$.items[0].email").value("marta@esibuy.test"))
                .andExpect(jsonPath("$.items[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.items[0].nombreComercial").value("Tienda Marta"))
                .andExpect(jsonPath("$.items[0].categoriaPrincipalId").value("cat-1"))
                .andExpect(jsonPath("$.items[0].categoriaPrincipal").value("Tecnología"))
                .andExpect(jsonPath("$.items[0].passwordHash").doesNotExist())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(10))
                .andExpect(jsonPath("$.totalItems").value(11))
                .andExpect(jsonPath("$.totalPages").value(2));

        verify(listadoUsuarios).listar(new ListarUsuariosQuery(
                "marta", Rol.VENDEDOR, "PENDIENTE", 1, 10, null, false));
    }

    // Una celda vacía llega como null: sin parámetro de orden o sin orden esperado.
    @ParameterizedTest(name = "sort={0}, order={1}")
    @CsvSource({
            "name,     asc,  NOMBRE, false",
            "status,   desc, ESTADO, true",
            "category, asc,  ,       false",
            ",         ,     ,       false"
    })
    @DisplayName("HU26-CP02: el listado de vendedores traduce la ordenación pedida")
    @WithMockUser(roles = "ADMINISTRADOR")
    void listadoDeVendedoresTraduceLaOrdenacion(String sort, String order, ListarUsuariosQuery.Orden orden,
            boolean descendente) throws Exception {
        when(listadoUsuarios.listar(any())).thenReturn(new UsuarioPage(java.util.List.of(), 0, 10, 0));
        MockHttpServletRequestBuilder peticion = get("/api/admin/sellers");
        if (sort != null) {
            peticion.param("sort", sort);
        }
        if (order != null) {
            peticion.param("order", order);
        }

        mockMvc.perform(peticion).andExpect(status().isOk());

        verify(listadoUsuarios).listar(new ListarUsuariosQuery(
                null, Rol.VENDEDOR, null, 0, 10, orden, descendente));
    }

    @Test
    @DisplayName("HU26-CP03: la ficha de un vendedor trae sus datos, sin datos sensibles")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnVendedor() throws Exception {
        when(administracionUsuarios.consultarVendedor("v-1")).thenReturn(vendedor());
        when(consultaCategorias.listar()).thenReturn(java.util.List.of(new Categoria("cat-1", "Tecnología")));

        mockMvc.perform(get("/api/admin/sellers/{id}", "v-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.nombre").value("Marta"))
                .andExpect(jsonPath("$.apellidos").value("Ruiz Gil"))
                .andExpect(jsonPath("$.email").value("marta@esibuy.test"))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Marta"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("cat-1"))
                .andExpect(jsonPath("$.categoriaPrincipal").value("Tecnología"))
                .andExpect(jsonPath("$.dni").value("87654321X"))
                .andExpect(jsonPath("$.telefono").value("612345678"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @DisplayName("HU26-CP04: la ficha de un usuario que no existe o no es vendedor devuelve 404")
    @WithMockUser(roles = "ADMINISTRADOR")
    void fichaDeUnNoVendedorDevuelve404() throws Exception {
        when(administracionUsuarios.consultarVendedor("usr-4"))
                .thenThrow(new UsuarioNoEncontradoException("El vendedor no existe"));

        mockMvc.perform(get("/api/admin/sellers/{id}", "usr-4"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"/api/admin/sellers", "/api/admin/sellers/v-1"})
    @DisplayName("HU26-CP05: sin sesión no se ven los vendedores")
    void vendedoresSinSesionDevuelve401(String ruta) throws Exception {
        mockMvc.perform(get(ruta))
                .andExpect(status().isUnauthorized());

        verify(listadoUsuarios, never()).listar(any());
        verify(administracionUsuarios, never()).consultarVendedor(any());
    }

    @ParameterizedTest(name = "rol {0} en {1}")
    @CsvSource({
            "CLIENTE,  /api/admin/sellers",
            "CLIENTE,  /api/admin/sellers/v-1",
            "VENDEDOR, /api/admin/sellers",
            "VENDEDOR, /api/admin/sellers/v-1"
    })
    @DisplayName("HU26-CP05: un usuario que no es administrador no ve los vendedores")
    void noAdministradorNoVeLosVendedores(String rol, String ruta) throws Exception {
        mockMvc.perform(get(ruta).with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(listadoUsuarios, never()).listar(any());
        verify(administracionUsuarios, never()).consultarVendedor(any());
    }

    // Activar y desactivar un vendedor desde su supervisión (HU21, T47): PATCH /api/admin/sellers/{id}/status,
    // contrato de frontend/docs/hu21-alta-admin-y-vendedores.md. Responde con los datos del vendedor.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU21-CP13 a CP17, propuestos.

    @Test
    @DisplayName("HU21-CP13: un administrador activa un vendedor y recibe sus datos con estado ACTIVO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorActivaUnVendedorYRecibeSusDatos() throws Exception {
        Usuario activado = vendedor();
        activado.activar();
        when(administracionUsuarios.activarVendedor("v-1")).thenReturn(activado);
        when(consultaCategorias.listar()).thenReturn(java.util.List.of(new Categoria("cat-1", "Tecnología")));

        mockMvc.perform(cambiarEstadoVendedor("v-1", "ACTIVO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Marta"))
                .andExpect(jsonPath("$.categoriaPrincipal").value("Tecnología"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(administracionUsuarios).activarVendedor("v-1");
        verify(administracionUsuarios, never()).desactivarVendedor(any());
    }

    @Test
    @DisplayName("HU21-CP14: un administrador desactiva un vendedor y recibe sus datos con estado DESACTIVADO")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorDesactivaUnVendedorYRecibeSusDatos() throws Exception {
        Usuario desactivado = vendedor();
        desactivado.desactivar();
        when(administracionUsuarios.desactivarVendedor("v-1")).thenReturn(desactivado);

        mockMvc.perform(cambiarEstadoVendedor("v-1", "DESACTIVADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.estado").value("DESACTIVADO"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        verify(administracionUsuarios).desactivarVendedor("v-1");
        verify(administracionUsuarios, never()).activarVendedor(any());
    }

    @Test
    @DisplayName("HU21-CP15: cambiar un vendedor a un estado que no es ACTIVO ni DESACTIVADO devuelve 400 y no cambia nada")
    @WithMockUser(roles = "ADMINISTRADOR")
    void cambiarVendedorAUnEstadoNoAdmitidoDevuelve400() throws Exception {
        mockMvc.perform(cambiarEstadoVendedor("v-1", "PENDIENTE"))
                .andExpect(status().isBadRequest());

        verify(administracionUsuarios, never()).activarVendedor(any());
        verify(administracionUsuarios, never()).desactivarVendedor(any());
    }

    @Test
    @DisplayName("HU21-CP16: cambiar el estado de un usuario que no existe o no es vendedor devuelve 404")
    @WithMockUser(roles = "ADMINISTRADOR")
    void cambiarEstadoDeUnNoVendedorDevuelve404() throws Exception {
        when(administracionUsuarios.activarVendedor("usr-4"))
                .thenThrow(new UsuarioNoEncontradoException("El vendedor no existe"));

        mockMvc.perform(cambiarEstadoVendedor("usr-4", "ACTIVO"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU21-CP17: sin sesión no se puede cambiar el estado de un vendedor")
    void cambiarEstadoDeVendedorSinSesionDevuelve401() throws Exception {
        mockMvc.perform(cambiarEstadoVendedor("v-1", "ACTIVO"))
                .andExpect(status().isUnauthorized());

        verify(administracionUsuarios, never()).activarVendedor(any());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU21-CP17: un usuario que no es administrador no puede cambiar el estado de un vendedor")
    void noAdministradorNoCambiaElEstadoDeUnVendedor(String rol) throws Exception {
        mockMvc.perform(cambiarEstadoVendedor("v-1", "ACTIVO").with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).activarVendedor(any());
    }

    // PATCH con sesión: lleva el token CSRF, como el de /api/admin/usuarios/{id}/status.
    private static MockHttpServletRequestBuilder cambiarEstadoVendedor(String id, String estado) {
        return patch("/api/admin/sellers/{id}/status", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"" + estado + "\"}");
    }

    // Editar un usuario (HU22, T42/T44): PUT /api/admin/usuarios/{id}, contrato de frontend/docs/hu22-editar-usuario.md.

    private static final String CUERPO_CLIENTE = """
            {"nombre":"Carla","apellidos":"Moreno Gil","dni":"10004119A",
             "telefono":"620023757","fechaNacimiento":"1978-04-13","tipoCliente":"PREMIUM","avatar":"avatar1"}""";

    private static final String CUERPO_VENDEDOR = """
            {"nombre":"Marta","apellidos":"Ruiz Gil","dni":"87654321X","telefono":"612345678",
             "nombreComercial":"Tienda Marta","categoriaPrincipalId":"cat-1"}""";

    @Test
    @DisplayName("HU22-CP01: el administrador edita un cliente y recibe su ficha actualizada")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorEditaUnClienteYRecibeSuFicha() throws Exception {
        when(administracionUsuarios.editar(eq("usr-1"), any())).thenReturn(clienteCarla());

        mockMvc.perform(editar("usr-1", CUERPO_CLIENTE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("usr-1"))
                .andExpect(jsonPath("$.nombre").value("Carla"))
                .andExpect(jsonPath("$.dni").value("10004119A"))
                .andExpect(jsonPath("$.avatar").value("avatar1"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        ArgumentCaptor<EditarUsuarioCommand> comando = ArgumentCaptor.forClass(EditarUsuarioCommand.class);
        verify(administracionUsuarios).editar(eq("usr-1"), comando.capture());
        assertEquals(new EditarUsuarioCommand("Carla", "Moreno Gil", "avatar1", "10004119A",
                "620023757", LocalDate.of(1978, 4, 13), TipoCliente.PREMIUM, null, null), comando.getValue());
    }

    @Test
    @DisplayName("HU22-CP03: el administrador edita un vendedor con sus datos de vendedor y recibe su ficha")
    @WithMockUser(roles = "ADMINISTRADOR")
    void administradorEditaUnVendedorYRecibeSuFicha() throws Exception {
        when(administracionUsuarios.editar(eq("v-1"), any())).thenReturn(vendedor());

        mockMvc.perform(editar("v-1", CUERPO_VENDEDOR))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombreComercial").value("Tienda Marta"))
                .andExpect(jsonPath("$.categoriaPrincipalId").value("cat-1"));

        ArgumentCaptor<EditarUsuarioCommand> comando = ArgumentCaptor.forClass(EditarUsuarioCommand.class);
        verify(administracionUsuarios).editar(eq("v-1"), comando.capture());
        assertEquals(new EditarUsuarioCommand("Marta", "Ruiz Gil", null, "87654321X", "612345678",
                null, null, "Tienda Marta", "cat-1"), comando.getValue());
    }

    @Test
    @DisplayName("HU22-CP06: editar con el nombre comercial de otro vendedor devuelve 409 con COMMERCIAL_NAME_ALREADY_REGISTERED")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarConElNombreComercialDeOtroDevuelve409() throws Exception {
        when(administracionUsuarios.editar(eq("v-1"), any()))
                .thenThrow(new NombreComercialYaRegistradoException("El nombre comercial ya está registrado"));

        mockMvc.perform(editar("v-1", CUERPO_VENDEDOR))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMERCIAL_NAME_ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("HU22-CP04: el email, la contraseña y el estado del cuerpo se ignoran")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarIgnoraEmailContrasenaYEstadoDelCuerpo() throws Exception {
        when(administracionUsuarios.editar(eq("usr-1"), any())).thenReturn(clienteCarla());
        String cuerpo = CUERPO_CLIENTE.replace("}",
                ",\"email\":\"otro@esibuy.test\",\"password\":\"Nueva-Clave-1!\",\"estado\":\"DESACTIVADO\"}");

        mockMvc.perform(editar("usr-1", cuerpo))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("carla@esibuy.test"));

        ArgumentCaptor<EditarUsuarioCommand> comando = ArgumentCaptor.forClass(EditarUsuarioCommand.class);
        verify(administracionUsuarios).editar(eq("usr-1"), comando.capture());
        assertEquals("Carla", comando.getValue().nombre());   // el resto del cuerpo sí llega
    }

    // El rol de una cuenta no cambia nunca ("el que nace cliente muere cliente", decisión del equipo).
    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "sin rol | {\"nombre\":\"Carla\",\"apellidos\":\"Moreno Gil\"}",
            "con otro rol | {\"rol\":\"VENDEDOR\",\"nombre\":\"Carla\",\"apellidos\":\"Moreno Gil\"}",
            "con un rol inexistente | {\"rol\":\"SUPERUSUARIO\",\"nombre\":\"Carla\",\"apellidos\":\"Moreno Gil\"}"})
    @DisplayName("HU22-CP04: el rol del cuerpo se ignora: sin rol o con otro rol se edita igual")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarIgnoraElRolDelCuerpo(String caso, String cuerpo) throws Exception {
        when(administracionUsuarios.editar(eq("usr-1"), any())).thenReturn(clienteCarla());

        mockMvc.perform(editar("usr-1", cuerpo))
                .andExpect(status().isOk());

        ArgumentCaptor<EditarUsuarioCommand> comando = ArgumentCaptor.forClass(EditarUsuarioCommand.class);
        verify(administracionUsuarios).editar(eq("usr-1"), comando.capture());
        assertEquals("Carla", comando.getValue().nombre());
    }

    @ParameterizedTest(name = "{0}")
    @CsvSource(delimiter = '|', value = {
            "nombre vacío | {\"nombre\":\"\",\"apellidos\":\"Moreno Gil\"}",
            "nombre de 31 caracteres | {\"nombre\":\"JJJJJJJJJJJJJJJJJJJJJJJJJJJJJJJ\",\"apellidos\":\"Moreno Gil\"}",
            "apellidos vacíos | {\"nombre\":\"Carla\",\"apellidos\":\"  \"}"})
    @DisplayName("HU22-CP05: rechaza un cuerpo con nombre o apellidos no válidos (400) sin llamar al caso de uso")
    @WithMockUser(roles = "ADMINISTRADOR")
    void rechazaUnCuerpoDeEdicionNoValido(String caso, String cuerpo) throws Exception {
        mockMvc.perform(editar("usr-1", cuerpo))
                .andExpect(status().isBadRequest());

        verify(administracionUsuarios, never()).editar(any(), any());
    }

    @Test
    @DisplayName("HU22-CP05: datos que el dominio rechaza (p. ej. un menor de edad) devuelven 400")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarConDatosQueElDominioRechazaDevuelve400() throws Exception {
        when(administracionUsuarios.editar(eq("usr-1"), any()))
                .thenThrow(new UsuarioInvalidoException("El cliente debe ser mayor de edad"));

        mockMvc.perform(editar("usr-1", CUERPO_CLIENTE))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @Test
    @DisplayName("HU22-CP06: editar con el DNI de otra cuenta devuelve 409 con DNI_ALREADY_REGISTERED")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarConElDniDeOtraCuentaDevuelve409() throws Exception {
        when(administracionUsuarios.editar(eq("usr-1"), any()))
                .thenThrow(new DniYaRegistradoException("El DNI ya está registrado"));

        mockMvc.perform(editar("usr-1", CUERPO_CLIENTE))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("DNI_ALREADY_REGISTERED"));
    }

    @Test
    @DisplayName("HU22-CP07: editar un usuario que no existe devuelve 404")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarUnUsuarioInexistenteDevuelve404() throws Exception {
        when(administracionUsuarios.editar(eq("no-existe"), any()))
                .thenThrow(new UsuarioNoEncontradoException("El usuario no existe"));

        mockMvc.perform(editar("no-existe", CUERPO_CLIENTE))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.mensaje").exists());
    }

    @ParameterizedTest(name = "rol {0}")
    @ValueSource(strings = {"CLIENTE", "VENDEDOR"})
    @DisplayName("HU22-CP08: un usuario que no es administrador no puede editar usuarios (403)")
    void unNoAdministradorNoPuedeEditarUsuarios(String rol) throws Exception {
        mockMvc.perform(editar("usr-1", CUERPO_CLIENTE).with(user("usuario").roles(rol)))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).editar(any(), any());
    }

    @Test
    @DisplayName("HU22-CP08: sin sesión no se puede editar un usuario (401)")
    void editarSinSesionEstaProtegido() throws Exception {
        mockMvc.perform(editar("usr-1", CUERPO_CLIENTE))
                .andExpect(status().isUnauthorized());

        verify(administracionUsuarios, never()).editar(any(), any());
    }

    @Test
    @DisplayName("HU22-CP08: un administrador sin token CSRF no puede editar un usuario (403)")
    @WithMockUser(roles = "ADMINISTRADOR")
    void editarSinTokenCsrfSeRechaza() throws Exception {
        mockMvc.perform(put("/api/admin/usuarios/{id}", "usr-1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CUERPO_CLIENTE))
                .andExpect(status().isForbidden());

        verify(administracionUsuarios, never()).editar(any(), any());
    }

    private static Usuario clienteCarla() {
        return new Usuario("usr-1", new DatosPersonales("Carla", "Moreno Gil", "avatar1"),
                new Email("carla@esibuy.test"), "hash-no-debe-salir", Rol.CLIENTE,
                new DatosCliente(TipoCliente.PREMIUM, new Dni("10004119A"), LocalDate.of(1978, 4, 13),
                        new Telefono("620023757")), null);
    }

    // PUT con sesión: lleva el token CSRF, como el PATCH de la HU24.
    private static MockHttpServletRequestBuilder editar(String id, String cuerpo) {
        return put("/api/admin/usuarios/{id}", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(cuerpo);
    }

    // PATCH con sesión: lleva el token CSRF (la estrategia de CSRF se decidirá con el login, task 230).
    private static MockHttpServletRequestBuilder cambiarEstado(String id, String estado) {
        return patch("/api/admin/usuarios/{id}/status", id).with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"estado\":\"" + estado + "\"}");
    }

    // Registro del vendedor (T241).

    @Test
    @DisplayName("HU1-CP19: un registro de vendedor válido devuelve 201")
    void registroDeVendedorValidoDevuelve201() throws Exception {
        mockMvc.perform(post(URL_REGISTRO_VENDEDOR).contentType(MediaType.APPLICATION_JSON).content(jsonVendedor()))
                .andExpect(status().isCreated());

        verify(registroUsuario).registrarVendedor(new RegistrarVendedorCommand("Luis", "Pérez García", "Tienda Luis",
                "cat-1", "87654321X", "luis@uclm.es", "612345678", "Clave-1234", "Clave-1234", "avatar1", true));
    }

    @Test
    @DisplayName("HU1-CP21: un vendedor sin categoría principal devuelve 400")
    void vendedorSinCategoriaDevuelve400() throws Exception {
        // La categoría es obligatoria en el formulario; la capa web la comprueba antes de llegar al caso de uso
        String cuerpo = jsonVendedor().replace("\"categoriaPrincipalId\": \"cat-1\"", "\"categoriaPrincipalId\": \"\"");

        mockMvc.perform(post(URL_REGISTRO_VENDEDOR).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest());

        verify(registroUsuario, never()).registrarVendedor(any());
    }

    @Test
    @DisplayName("HU1-CP20: un nombre comercial ya registrado devuelve 409 con su código")
    void nombreComercialYaRegistradoDevuelve409ConCodigo() throws Exception {
        doThrow(new NombreComercialYaRegistradoException("El nombre comercial ya está registrado"))
                .when(registroUsuario).registrarVendedor(any());

        mockMvc.perform(post(URL_REGISTRO_VENDEDOR).contentType(MediaType.APPLICATION_JSON).content(jsonVendedor()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("COMMERCIAL_NAME_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("Tienda Luis"))));
    }

    @Test
    @DisplayName("HU1-CP20: correo y nombre comercial ya registrados devuelven 409 con su código")
    void correoYNombreComercialYaRegistradosDevuelve409ConCodigo() throws Exception {
        doThrow(new CorreoYNombreComercialYaRegistradosException("El correo y el nombre comercial ya están registrados"))
                .when(registroUsuario).registrarVendedor(any());

        mockMvc.perform(post(URL_REGISTRO_VENDEDOR).contentType(MediaType.APPLICATION_JSON).content(jsonVendedor()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_AND_COMMERCIAL_NAME_ALREADY_REGISTERED"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("luis@uclm.es"))));
    }

    @Test
    @DisplayName("HU21-CP06: crear administrador con datos correctos devuelve 201")
    void crearAdministradorValidoDevuelve201() throws Exception {
        String peticionJson = """
            {
              "nombre": "Nora",
              "apellidos": "Díaz Ruiz",
              "email": "nora@esibuy.test",
              "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/admin/admins")
                .with(user("admin").roles("ADMINISTRADOR")) 
                .with(csrf()) 
                .contentType(MediaType.APPLICATION_JSON)
                .content(peticionJson))
                .andExpect(status().isCreated());

        verify(crearAdministrador).crear(any());
    }

    @Test
    @DisplayName("HU21-CP18: crear administrador pasa al caso de uso el avatar elegido")
    void crearAdministradorPasaElAvatar() throws Exception {
        String peticionJson = """
            {
              "nombre": "Nora",
              "apellidos": "Díaz Ruiz",
              "email": "nora@esibuy.test",
              "sede": "Ciudad Real",
              "avatar": "avatar2",
              "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/admin/admins")
                .with(user("admin").roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(peticionJson))
                .andExpect(status().isCreated());

        verify(crearAdministrador).crear(new CrearAdministradorCommand(
                "Nora", "Díaz Ruiz", "nora@esibuy.test", "avatar2", "Password123!"));
    }

    @Test
    @DisplayName("HU21-CP07: crear administrador rechaza campos obligatorios vacíos devolviendo 400")
    void crearAdministradorRechazaCamposVacios() throws Exception {
        String peticionInvalidaJson = """
            {
              "nombre": "",
              "email": "correo-sin-arroba",
              "password": ""
            }
            """;

        mockMvc.perform(post("/api/admin/admins")
                .with(user("admin").roles("ADMINISTRADOR"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(peticionInvalidaJson))
                .andExpect(status().isBadRequest());

        verify(crearAdministrador, never()).crear(any());
    }

    @Test
    @DisplayName("HU21-CP08: usuarios sin rol ADMINISTRADOR no pueden crear administradores")
    void crearAdministradorRechazaNoAdministradores() throws Exception {
        String peticionJson = """
            {
              "nombre": "Nora",
              "apellidos": "Díaz Ruiz",
              "email": "nora@esibuy.test",
              "password": "Password123!"
            }
            """;

        mockMvc.perform(post("/api/admin/admins")
                .with(user("cliente").roles("CLIENTE"))
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(peticionJson))
                .andExpect(status().isForbidden());

        verify(crearAdministrador, never()).crear(any());
    }

    // Fallos de la demo del Sprint 1: cada 400 del registro lleva un code para que el frontend marque el campo.

    @Test
    @DisplayName("HU1-CP22: una contraseña con datos personales devuelve 400 con su código")
    void contrasenaConDatosPersonalesDevuelve400ConSuCodigo() throws Exception {
        doThrow(new ContrasenaInvalidaException(MotivoRechazo.CONTRASENA_CON_DATOS_PERSONALES,
                "La contraseña no puede contener datos personales"))
                .when(registroUsuario).registrarCliente(any());

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json("juan@uclm.es", "clave-Juan-2026")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_CONTAINS_PERSONAL_DATA"))
                .andExpect(jsonPath("$.mensaje").value("Datos de registro no válidos"));
    }

    @Test
    @DisplayName("HU1-CP22: un DNI inválido devuelve 400 con su código")
    void dniInvalidoDevuelve400ConSuCodigo() throws Exception {
        doThrow(new UsuarioInvalidoException(MotivoRechazo.DNI_INVALIDO, "El DNI no tiene un formato válido"))
                .when(registroUsuario).registrarCliente(any());

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json("juan@uclm.es", "Clave-1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_DNI"));
    }

    @Test
    @DisplayName("HU1-CP22: un email inválido devuelve 400 con su código")
    void emailInvalidoDevuelve400ConSuCodigo() throws Exception {
        doThrow(new EmailInvalidoException("El email no tiene un formato válido: ana@localhost"))
                .when(registroUsuario).registrarCliente(any());

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json("ana@localhost", "Clave-1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_EMAIL"))
                .andExpect(jsonPath("$.mensaje").value(not(containsString("ana@localhost"))));
    }

    @Test
    @DisplayName("HU1-CP22: un dato inválido sin motivo concreto devuelve 400 con INVALID_FIELD")
    void datoInvalidoGenericoDevuelve400ConInvalidField() throws Exception {
        doThrow(new UsuarioInvalidoException("Un cliente debe tener datos de cliente"))
                .when(registroUsuario).registrarCliente(any());

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(json("juan@uclm.es", "Clave-1234")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FIELD"));
    }

    @Test
    @DisplayName("HU1-CP22: un error de validación del formulario devuelve 400 con INVALID_FIELD y mensaje genérico")
    void errorDeValidacionDelFormularioDevuelve400ConInvalidField() throws Exception {
        // La capa web (@Valid) rechaza el nombre vacío antes del caso de uso: también con el formato de error de la API
        String cuerpo = json("juan@uclm.es", "Clave-1234").replace("\"nombre\": \"Juan\"", "\"nombre\": \"\"");

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_FIELD"))
                .andExpect(jsonPath("$.mensaje").value("Datos de registro no válidos"));

        verify(registroUsuario, never()).registrarCliente(any());
    }

    // Fallos de la demo del Sprint 1: el formulario envía la imagen como "avatar", igual que el resto de la API.

    @Test
    @DisplayName("HU1-CP01: el registro del cliente guarda el avatar elegido")
    void elRegistroDelClienteGuardaElAvatar() throws Exception {
        String cuerpo = json("juan@uclm.es", "Clave-1234").replace("\"avatar\": null", "\"avatar\": \"avatar2\"");

        mockMvc.perform(post(URL_REGISTRO).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegistrarClienteCommand> comando = ArgumentCaptor.forClass(RegistrarClienteCommand.class);
        verify(registroUsuario).registrarCliente(comando.capture());
        assertEquals("avatar2", comando.getValue().imagenPerfil());
    }

    @Test
    @DisplayName("HU1-CP19: el registro del vendedor guarda el avatar elegido")
    void elRegistroDelVendedorGuardaElAvatar() throws Exception {
        String cuerpo = jsonVendedor().replace("\"avatar\": \"avatar1\"", "\"avatar\": \"avatar3\"");

        mockMvc.perform(post(URL_REGISTRO_VENDEDOR).contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isCreated());

        ArgumentCaptor<RegistrarVendedorCommand> comando = ArgumentCaptor.forClass(RegistrarVendedorCommand.class);
        verify(registroUsuario).registrarVendedor(comando.capture());
        assertEquals("avatar3", comando.getValue().imagenPerfil());
    }

    // Fallos de la demo del Sprint 1: el listado de clientes y administradores no ordenaba (HU19).

    @Test
    @DisplayName("HU19-CP11: el listado de usuarios acepta el orden y el sentido, como el de vendedores")
    @WithMockUser(roles = "ADMINISTRADOR")
    void elListadoDeUsuariosAceptaOrdenYSentido() throws Exception {
        when(listadoUsuarios.listar(any())).thenReturn(new UsuarioPage(java.util.List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/admin/usuarios")
                        .param("role", "CLIENTE")
                        .param("sort", "status")
                        .param("order", "desc"))
                .andExpect(status().isOk());

        verify(listadoUsuarios).listar(new ListarUsuariosQuery(
                null, Rol.CLIENTE, null, 0, 10, ListarUsuariosQuery.Orden.ESTADO, true));
    }

    // Fallos de la demo del Sprint 1: el listado de vendedores ignoraba la categoría (HU26).

    @Test
    @DisplayName("HU26-CP06: el listado de vendedores filtra por categoría")
    @WithMockUser(roles = "ADMINISTRADOR")
    void elListadoDeVendedoresFiltraPorCategoria() throws Exception {
        when(listadoUsuarios.listar(any())).thenReturn(new UsuarioPage(java.util.List.of(), 0, 10, 0));

        mockMvc.perform(get("/api/admin/sellers").param("category", "cat-1"))
                .andExpect(status().isOk());

        verify(listadoUsuarios).listar(new ListarUsuariosQuery(
                null, Rol.VENDEDOR, null, "cat-1", 0, 10, null));
    }
}
