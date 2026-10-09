package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.EnvioEmailException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaBloqueadaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CuentaDesactivadaException;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
}
