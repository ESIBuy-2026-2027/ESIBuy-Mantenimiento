package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;
import es.uclm.esibuy.backend.preferencias.domain.exception.CategoriaNoDisponibleException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Preferencias del cliente (HU46, T287): GET y PUT /api/customer/preferences; y sus categorías favoritas (AB#528): POST y
// DELETE /api/customer/preferences/categories. El caso de uso es un doble de prueba; la seguridad es la real
// (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF) y los errores los traduce ManejadorGlobalErrores.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU46-CP14 a CP18 y HU528-CP08 a CP11, propuestos.
@WebMvcTest(PreferenciasController.class)
@Import(SecurityConfig.class)
class PreferenciasControllerTest {

    private static final String URL = "/api/customer/preferences";
    private static final String URL_CATEGORIAS = URL + "/categories";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String CUERPO_MARCAR = "{\"categoriaId\": \"cat-1\"}";
    private static final String CUERPO = "{\"categorias\": [\"cat-2\", \"cat-1\"]}";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionPreferenciasUseCase gestionPreferencias;

    private static MockHttpServletRequestBuilder guardar(String cuerpo) {
        return put(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static MockHttpServletRequestBuilder marcar(String cuerpo) {
        return post(URL_CATEGORIAS).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(cuerpo);
    }

    private static MockHttpServletRequestBuilder desmarcar() {
        return delete(URL_CATEGORIAS + "/cat-1").with(csrf());
    }

    @Test
    @DisplayName("HU528-CP08: marcar envía la categoría del cuerpo para el cliente en sesión y responde 200 con sus favoritas")
    void marcarRespondeConLasFavoritas() throws Exception {
        when(gestionPreferencias.marcar(CLIENTE, "cat-1")).thenReturn(List.of("cat-2", "cat-1"));

        mockMvc.perform(marcar(CUERPO_MARCAR).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(2))
                .andExpect(jsonPath("$.categorias[0]").value("cat-2"))
                .andExpect(jsonPath("$.categorias[1]").value("cat-1"));
    }

    @Test
    @DisplayName("HU528-CP08: el cliente sale de la sesión: un clienteId en el cuerpo se ignora")
    void marcarIgnoraElClienteDelCuerpo() throws Exception {
        when(gestionPreferencias.marcar(CLIENTE, "cat-1")).thenReturn(List.of("cat-1"));

        mockMvc.perform(marcar("{\"categoriaId\": \"cat-1\", \"clienteId\": \"otro\"}")
                        .with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk());

        verify(gestionPreferencias).marcar(CLIENTE, "cat-1");
    }

    @Test
    @DisplayName("HU528-CP09: desmarcar quita la categoría de la ruta al cliente en sesión y responde 200 con sus favoritas")
    void desmarcarRespondeConLasFavoritas() throws Exception {
        when(gestionPreferencias.desmarcar(CLIENTE, "cat-1")).thenReturn(List.of("cat-2"));

        mockMvc.perform(desmarcar().with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.categorias.length()").value(1))
                .andExpect(jsonPath("$.categorias[0]").value("cat-2"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"{}", "{\"categoriaId\": null}", "{\"categoriaId\": \"  \"}",
            "{\"categoriaId\": [\"cat-1\"]}", "{\"categoriaId\": "})
    @DisplayName("HU528-CP10: marcar sin categoría, en blanco, con una lista o con un cuerpo ilegible devuelve 400 y no llega al caso de uso")
    void marcarConUnCuerpoInvalidoDevuelve400(String cuerpo) throws Exception {
        mockMvc.perform(marcar(cuerpo).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verifyNoInteractions(gestionPreferencias);
    }

    @Test
    @DisplayName("HU528-CP11: marcar una categoría inactiva o que no existe devuelve 404 CATEGORY_NOT_FOUND con un mensaje genérico")
    void marcarUnaCategoriaNoDisponibleDevuelve404() throws Exception {
        when(gestionPreferencias.marcar(CLIENTE, "cat-1"))
                .thenThrow(new CategoriaNoDisponibleException("La categoría no está disponible"));

        mockMvc.perform(marcar(CUERPO_MARCAR).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CATEGORY_NOT_FOUND"))
                .andExpect(jsonPath("$.mensaje").value("La categoría no existe"));
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
