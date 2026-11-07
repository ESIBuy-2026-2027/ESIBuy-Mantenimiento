package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.domain.exception.PreferenciasInvalidasException;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Preferencias del cliente (HU46, T287): GET y PUT /api/customer/preferences. El caso de uso es un doble de prueba; la
// seguridad es la real (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF) y los errores los traduce
// ManejadorGlobalErrores.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP14 a CP18, propuestos.
@WebMvcTest(PreferenciasController.class)
@Import(SecurityConfig.class)
class PreferenciasControllerTest {

    private static final String URL = "/api/customer/preferences";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String CUERPO = "{\"categorias\": [\"cat-2\", \"cat-1\"]}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionPreferenciasUseCase gestionPreferencias;

    private static MockHttpServletRequestBuilder guardar(String cuerpo) {
        return put(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    @Test
    @DisplayName("HU46-CP14: consultar responde 200 con las categorías del cliente en sesión")
    void consultarRespondeConLasCategoriasDelCliente() throws Exception {
        when(gestionPreferencias.consultar(CLIENTE)).thenReturn(List.of("cat-2", "cat-1"));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(2))
                .andExpect(jsonPath("$.categorias[0]").value("cat-2"))
                .andExpect(jsonPath("$.categorias[1]").value("cat-1"));
    }

    @Test
    @DisplayName("HU46-CP15: guardar envía las categorías del cuerpo para el cliente en sesión y responde 200 con las guardadas")
    void guardarRespondeConLasCategoriasGuardadas() throws Exception {
        when(gestionPreferencias.guardar(CLIENTE, List.of("cat-2", "cat-1"))).thenReturn(List.of("cat-2", "cat-1"));

        mockMvc.perform(guardar(CUERPO).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(2))
                .andExpect(jsonPath("$.categorias[0]").value("cat-2"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"{}", "{\"categorias\": null}", "{\"categorias\": \"cat-1\"}", "{\"categorias\": "})
    @DisplayName("HU46-CP16: un cuerpo sin categorías, con categorías que no son una lista o ilegible devuelve 400 y no llega al caso de uso")
    void cuerpoInvalidoNoLlegaAlCasoDeUso(String cuerpo) throws Exception {
        mockMvc.perform(guardar(cuerpo).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(gestionPreferencias);
    }

    @Test
    @DisplayName("HU46-CP17: guardar una categoría inactiva o que no existe devuelve 400")
    void unaCategoriaNoValidaDevuelve400() throws Exception {
        when(gestionPreferencias.guardar(any(), any()))
                .thenThrow(new PreferenciasInvalidasException("Alguna categoría no existe o no está activa"));

        mockMvc.perform(guardar(CUERPO).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU46-CP18: sin sesión no se consultan ni se guardan las preferencias (401)")
    void sinSesionNoSeUsanLasPreferencias() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(guardar(CUERPO)).andExpect(status().isUnauthorized());

        verifyNoInteractions(gestionPreferencias);
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"VENDEDOR", "ADMINISTRADOR"})
    @DisplayName("HU46-CP18: un usuario que no es cliente no tiene preferencias (403)")
    void noClienteNoTienePreferencias(String rol) throws Exception {
        mockMvc.perform(get(URL).with(user(CLIENTE).roles(rol))).andExpect(status().isForbidden());
        mockMvc.perform(guardar(CUERPO).with(user(CLIENTE).roles(rol))).andExpect(status().isForbidden());

        verifyNoInteractions(gestionPreferencias);
    }

    @Test
    @DisplayName("HU46-CP18: una petición sin token CSRF no guarda las preferencias (403)")
    void sinTokenCsrfNoSeGuardan() throws Exception {
        mockMvc.perform(put(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());

        verify(gestionPreferencias, never()).guardar(any(), any());
    }
}
