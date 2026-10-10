package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.in.VerificarCodigoEmailCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import java.time.LocalDate;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests del adaptador web del login (HU2, T230). Siguen el contrato del frontend
// (frontend/docs/hu2-login.md, sección 5). Como UsuarioControllerTest, no levantan MongoDB:
// el caso de uso se sustituye por un doble, y se prueban también SecurityConfig y
// ManejadorGlobalErrores a través de los endpoints.
// Las peticiones llevan .with(csrf()): simula el token anti-CSRF que enviará el frontend (T230, D4).
@WebMvcTest(AutenticacionController.class)
@Import(SecurityConfig.class)
class AutenticacionControllerTest {

    private static final String URL_LOGIN = "/api/auth/login";
    private static final String URL_CODIGO_EMAIL = "/api/auth/verify-email-code";
    private static final String URL_ME = "/api/auth/me";
    private static final String URL_LOGOUT = "/api/auth/logout";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AutenticacionUseCase autenticacion;

    private static String jsonLogin(String email, String password) {
        return """
               {"email": "%s", "password": "%s"}
               """.formatted(email, password);
    }

    @Test
    @DisplayName("HU2-CP01: un login correcto devuelve 200 y pasa al paso del código del correo")
    void loginCorrectoPasaAlCodigoDelCorreo() throws Exception {
        when(autenticacion.iniciarSesion(any()))
                .thenReturn(new ResultadoInicioSesion("juan@uclm.es", PasoLogin.CODIGO_EMAIL));

        mockMvc.perform(post(URL_LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                .andExpect(status().isOk())
                // El frontend espera "emailCode" (authHandlers.js), no el nombre del enum
                .andExpect(jsonPath("$.nextStep").value("emailCode"));

        verify(autenticacion).iniciarSesion(new IniciarSesionCommand("juan@uclm.es", "clave1234"));
    }

    @Test
    @DisplayName("HU2-CP02: unas credenciales incorrectas devuelven 401 sin revelar si el correo existe")
    void loginConCredencialesIncorrectasDevuelve401() throws Exception {
        // El servicio lanza la misma excepción si el correo no existe o si la contraseña no coincide (T19)
        when(autenticacion.iniciarSesion(any()))
                .thenThrow(new CredencialesInvalidasException("Credenciales inválidas"));

        mockMvc.perform(post(URL_LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "otraClave")))
                .andExpect(status().isUnauthorized())
                // Mensaje genérico: la respuesta no devuelve el correo ni la contraseña enviados
                .andExpect(content().string(not(containsString("juan@uclm.es"))))
                .andExpect(content().string(not(containsString("otraClave"))));
    }

    @Test
    @DisplayName("HU2-CP03: una cuenta bloqueada por intentos fallidos devuelve 429")
    void cuentaBloqueadaDevuelve429() throws Exception {
        // El servicio la lanza durante los 15 minutos de bloqueo tras 5 intentos fallidos (T19)
        when(autenticacion.iniciarSesion(any()))
                .thenThrow(new CuentaBloqueadaException("Demasiados intentos. Inténtalo más tarde"));

        mockMvc.perform(post(URL_LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                // 429 Too Many Requests: el frontend lo muestra como «demasiados intentos» (hu2-login.md)
                .andExpect(status().isTooManyRequests());
    }

    @Test
    @DisplayName("HU2-CP04: una cuenta desactivada devuelve 403 con el código ACCOUNT_DISABLED")
    void cuentaDesactivadaDevuelve403ConCodigo() throws Exception {
        // El servicio solo la lanza cuando la contraseña es correcta (T19), como pide el frontend
        when(autenticacion.iniciarSesion(any()))
                .thenThrow(new CuentaDesactivadaException("Esta cuenta está desactivada"));

        mockMvc.perform(post(URL_LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                .andExpect(status().isForbidden())
                // El frontend lee "code" para mostrar «cuenta desactivada» (client.js, loginErrors.js)
                .andExpect(jsonPath("$.code").value("ACCOUNT_DISABLED"));
    }

    @Test
    @DisplayName("HU2-CP05: si no se puede enviar el código por correo, devuelve 503 sin detalles internos")
    void falloDelCorreoDevuelve503() throws Exception {
        // El adaptador de correo (T231) traduce el fallo de Brevo a EnvioEmailException y conserva la causa
        when(autenticacion.iniciarSesion(any()))
                .thenThrow(new EnvioEmailException("No se ha podido enviar el código de verificación",
                        new RuntimeException("535 Authentication failed")));

        mockMvc.perform(post(URL_LOGIN)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                // 503 Service Unavailable: el fallo es de un servicio externo, no de los datos del usuario
                .andExpect(status().isServiceUnavailable())
                // La causa (p. ej. el error SMTP) se queda en el log, nunca en la respuesta
                .andExpect(content().string(not(containsString("535"))));
    }

    @Test
    @DisplayName("HU2-CP06: un código correcto tras el login pasa al paso de Microsoft Authenticator")
    void codigoCorrectoPasaAlAuthenticator() throws Exception {
        when(autenticacion.iniciarSesion(any()))
                .thenReturn(new ResultadoInicioSesion("juan@uclm.es", PasoLogin.CODIGO_EMAIL));
        when(autenticacion.verificarCodigoEmail(any()))
                .thenReturn(new ResultadoInicioSesion("juan@uclm.es", PasoLogin.AUTHENTICATOR));
        // La misma sesión en las dos peticiones: es lo que hace el navegador con la cookie JSESSIONID
        MockHttpSession sesion = new MockHttpSession();

        // Paso 1: login correcto
        mockMvc.perform(post(URL_LOGIN)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                .andExpect(status().isOk());

        // Paso 2: el cuerpo solo lleva el código; el correo lo recuerda la sesión (T230, D3)
        mockMvc.perform(post(URL_CODIGO_EMAIL)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"code": "123456"}
                                 """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nextStep").value("authenticator"));

        verify(autenticacion).verificarCodigoEmail(new VerificarCodigoEmailCommand("juan@uclm.es", "123456"));
    }

    @Test
    @DisplayName("HU2-CP07: un código enviado sin haber hecho antes el login devuelve 401")
    void codigoSinLoginPrevioDevuelve401() throws Exception {
        // Sin el paso 1 la sesión no tiene correo: el backend impone el orden de los pasos (hu2-login.md)
        mockMvc.perform(post(URL_CODIGO_EMAIL)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"code": "123456"}
                                 """))
                .andExpect(status().isUnauthorized());

        // Sin saber de quién es el código no tiene sentido consultarlo
        verify(autenticacion, never()).verificarCodigoEmail(any());
    }

    @Test
    @DisplayName("HU2-CP07: un código rechazado por el servicio tras el login devuelve 401")
    void codigoIncorrectoDevuelve401() throws Exception {
        when(autenticacion.iniciarSesion(any()))
                .thenReturn(new ResultadoInicioSesion("juan@uclm.es", PasoLogin.CODIGO_EMAIL));
        // El servicio lanza la misma excepción si el código es incorrecto, ha caducado, ya se usó o está agotado
        when(autenticacion.verificarCodigoEmail(any()))
                .thenThrow(new CodigoInvalidoException("Código inválido"));
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(post(URL_LOGIN)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonLogin("juan@uclm.es", "clave1234")))
                .andExpect(status().isOk());

        mockMvc.perform(post(URL_CODIGO_EMAIL)
                        .session(sesion)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                 {"code": "000000"}
                                 """))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("HU2-CP14: sin sesión iniciada, /me devuelve 401")
    void meSinSesionDevuelve401() throws Exception {
        // El frontend lo llama al cargar la app (AuthProvider.jsx): 401 = visitante, no un error
        mockMvc.perform(get(URL_ME))
                .andExpect(status().isUnauthorized());

        verify(autenticacion, never()).consultarUsuarioEnSesion(any());
    }

    @Test
    @WithMockUser(username = "juan@uclm.es") // simula el usuario autenticado que dejará el paso 3 (T20)
    @DisplayName("HU2-CP14: con sesión iniciada, /me devuelve los datos del usuario y nada más")
    void meConSesionDevuelveElUsuario() throws Exception {
        Usuario usuario = new Usuario("u-1", new DatosPersonales("Juan", "García", null), new Email("juan@uclm.es"),
                "hash-guardado", Rol.CLIENTE, new DatosCliente(TipoCliente.ESTANDAR, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
        usuario.activar();   // un usuario con sesión es una cuenta verificada (una nueva nace PENDIENTE, T233)
        when(autenticacion.consultarUsuarioEnSesion("juan@uclm.es")).thenReturn(usuario);

        mockMvc.perform(get(URL_ME))
                .andExpect(status().isOk())
                // Contrato del frontend (hu2-login.md): { id, nombre, apellidos, email, rol, estado }
                .andExpect(jsonPath("$.id").value("u-1"))
                .andExpect(jsonPath("$.nombre").value("Juan"))
                .andExpect(jsonPath("$.apellidos").value("García"))
                .andExpect(jsonPath("$.email").value("juan@uclm.es"))
                .andExpect(jsonPath("$.rol").value("CLIENTE"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"))
                // El DTO elige qué sale: nunca el hash de la contraseña ni datos internos
                .andExpect(content().string(not(containsString("hash-guardado"))))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    @WithMockUser(username = "juan@uclm.es")
    @DisplayName("HU2-CP15: cerrar sesión devuelve 204 e invalida la sesión en el servidor")
    void logoutCierraLaSesion() throws Exception {
        MockHttpSession sesion = new MockHttpSession();

        mockMvc.perform(post(URL_LOGOUT)
                        .session(sesion)
                        .with(csrf()))
                .andExpect(status().isNoContent());

        // El frontend no puede borrar la cookie HttpOnly: es el servidor quien invalida la sesión,
        // y la cookie deja de valer aunque alguien la tenga
        assertTrue(sesion.isInvalid(), "La sesión debe quedar invalidada");
    }
}
