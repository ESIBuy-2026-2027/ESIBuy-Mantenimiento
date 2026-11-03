package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.usuarios.application.port.in.RecuperacionContrasenaUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.RestablecerContrasenaCommand;
import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Tests del adaptador web de la recuperación de contraseña (HU3, T399). Como AutenticacionControllerTest,
// no levantan MongoDB: el caso de uso se sustituye por un doble, y se prueban también SecurityConfig y
// ManejadorGlobalErrores a través de los endpoints.
// Las dos rutas son públicas (se usan sin sesión), pero piden el token anti-CSRF, como el login: .with(csrf()).
@WebMvcTest(RecuperacionContrasenaController.class)
@Import(SecurityConfig.class)
class RecuperacionContrasenaControllerTest {

    private static final String URL_SOLICITAR = "/api/auth/password-recovery";
    private static final String URL_RESTABLECER = "/api/auth/password-recovery/confirm";

    private static final String EMAIL = "juan@uclm.es";
    private static final String CODIGO = "654321";
    private static final String NUEVA = "Nueva-Clave-9!";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RecuperacionContrasenaUseCase recuperacion;

    private static String jsonSolicitar(String email) {
        return """
               {"email": "%s"}
               """.formatted(email);
    }

    private static String jsonRestablecer(String email, String code, String password) {
        return """
               {"email": "%s", "code": "%s", "password": "%s"}
               """.formatted(email, code, password);
    }

    // --- Paso 1: solicitar la recuperación ---------------------------------------------------------------------

    @Test
    @DisplayName("HU3-CP12: solicitar la recuperación responde 204 sin sesión y pasa el email al caso de uso")
    void solicitarRespondeSinContenidoYPasaElEmail() throws Exception {
        mockMvc.perform(post(URL_SOLICITAR).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitar(EMAIL)))
                .andExpect(status().isNoContent());

        verify(recuperacion).solicitarRecuperacion(EMAIL);
    }

    @Test
    @DisplayName("HU3-CP16: solicitar sin email responde 400 y no llama al caso de uso")
    void solicitarSinEmailRespondeBadRequest() throws Exception {
        mockMvc.perform(post(URL_SOLICITAR).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitar(" ")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(recuperacion);
    }

    @Test
    @DisplayName("HU3-CP16: solicitar sin el token anti-CSRF responde 403 y no llama al caso de uso")
    void solicitarSinCsrfRespondeForbidden() throws Exception {
        mockMvc.perform(post(URL_SOLICITAR)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonSolicitar(EMAIL)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recuperacion);
    }

    // --- Paso 2: restablecer la contraseña ---------------------------------------------------------------------

    @Test
    @DisplayName("HU3-CP13: restablecer con el código correcto responde 204 y pasa email, código y contraseña al caso de uso")
    void restablecerRespondeSinContenidoYPasaElComando() throws Exception {
        mockMvc.perform(post(URL_RESTABLECER).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, CODIGO, NUEVA)))
                .andExpect(status().isNoContent());

        verify(recuperacion).restablecerContrasena(new RestablecerContrasenaCommand(EMAIL, CODIGO, NUEVA));
    }

    @Test
    @DisplayName("HU3-CP14: restablecer con un código que no sirve responde 401 «Código incorrecto»")
    void restablecerConCodigoInvalidoRespondeUnauthorized() throws Exception {
        doThrow(new CodigoInvalidoException("Código inválido"))
                .when(recuperacion).restablecerContrasena(any());

        mockMvc.perform(post(URL_RESTABLECER).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, "000000", NUEVA)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.mensaje").value("Código incorrecto"));
    }

    @Test
    @DisplayName("HU3-CP15: restablecer con una contraseña que no cumple la política responde 400 con el código del motivo")
    void restablecerConContrasenaDebilRespondeBadRequestConSuCodigo() throws Exception {
        doThrow(new ContrasenaInvalidaException(MotivoRechazo.CONTRASENA_DEBIL, "La contraseña es débil"))
                .when(recuperacion).restablecerContrasena(any());

        mockMvc.perform(post(URL_RESTABLECER).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, CODIGO, "debil")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("PASSWORD_WEAK"));
    }

    @Test
    @DisplayName("HU3-CP16: restablecer sin código o sin contraseña responde 400 y no llama al caso de uso")
    void restablecerConCamposVaciosRespondeBadRequest() throws Exception {
        mockMvc.perform(post(URL_RESTABLECER).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, "", NUEVA)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        mockMvc.perform(post(URL_RESTABLECER).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, CODIGO, "")))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(recuperacion);
    }

    @Test
    @DisplayName("HU3-CP16: restablecer sin el token anti-CSRF responde 403 y no llama al caso de uso")
    void restablecerSinCsrfRespondeForbidden() throws Exception {
        mockMvc.perform(post(URL_RESTABLECER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRestablecer(EMAIL, CODIGO, NUEVA)))
                .andExpect(status().isForbidden());

        verifyNoInteractions(recuperacion);
    }
}
