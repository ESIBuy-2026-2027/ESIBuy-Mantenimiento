package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValoracionProductosUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.in.ValorarProductoCommand;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ProductoNoCompradoException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionInvalidaException;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Valoración de un producto (HU13, T370): POST /api/products/{productoId}/valoraciones. El caso de uso es un doble de
// prueba; la seguridad es la real (SecurityConfig: POST de valoraciones solo para CLIENTE, con CSRF).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP14, propuesto.
@WebMvcTest(ValoracionController.class)
@Import(SecurityConfig.class)
class ValoracionControllerTest {

    private static final String URL = "/api/products/p-1/valoraciones";
    private static final String CLIENTE = "cliente@esibuy.test";
    // clienteId en el cuerpo se ignora: el cliente sale siempre de la sesión
    private static final String CUERPO =
            "{\"puntuacion\": 4, \"comentario\": \"Buena calidad\", \"clienteId\": \"otro\"}";
    private static final ValorarProductoCommand COMANDO = new ValorarProductoCommand(CLIENTE, "p-1", 4,
            "Buena calidad");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ValoracionProductosUseCase valoracionProductos;

    private ResultActions valorar(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    @DisplayName("HU13-CP14: valorar un producto responde 201 con la valoración creada, con el cliente de la sesión")
    void valorarUnProductoResponde201() throws Exception {
        when(valoracionProductos.valorar(COMANDO)).thenReturn(new Valoracion("v-1", "p-1", "c-1", 4, "Buena calidad"));

        valorar(CUERPO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("v-1"))
                .andExpect(jsonPath("$.productoId").value("p-1"))
                .andExpect(jsonPath("$.puntuacion").value(4))
                .andExpect(jsonPath("$.comentario").value("Buena calidad"))
                .andExpect(jsonPath("$.clienteId").doesNotExist());
    }

    @Test
    @DisplayName("HU13-CP14: una valoración que no cumple las reglas (puntuación o comentario) responde 400")
    void unaValoracionInvalidaResponde400() throws Exception {
        when(valoracionProductos.valorar(any())).thenThrow(new ValoracionInvalidaException("no"));

        valorar("{\"puntuacion\": 7}")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU13-CP14: valorar un producto que no se ha comprado responde 403 con el code PRODUCT_NOT_PURCHASED")
    void valorarSinHaberCompradoResponde403() throws Exception {
        when(valoracionProductos.valorar(COMANDO)).thenThrow(new ProductoNoCompradoException());

        valorar(CUERPO)
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_PURCHASED"));
    }

    @Test
    @DisplayName("HU13-CP14: valorar dos veces el mismo producto responde 409 con el code REVIEW_ALREADY_EXISTS")
    void valorarDosVecesResponde409() throws Exception {
        when(valoracionProductos.valorar(COMANDO)).thenThrow(new ValoracionDuplicadaException());

        valorar(CUERPO)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("REVIEW_ALREADY_EXISTS"));
    }

    @Test
    @DisplayName("HU13-CP14: sin sesión responde 401, otro rol 403 y sin CSRF se rechaza")
    void soloElClienteValora() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(URL).with(user("vendedor@esibuy.test").roles("VENDEDOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());

        verify(valoracionProductos, never()).valorar(any());
    }
}
