package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.deseos.application.port.in.DeseoDetallado;
import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;
import es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
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

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Lista de deseos del cliente (HU14): POST /api/customer/wishlist/items. El caso de uso es un doble de prueba; la
// seguridad es la real (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF).
@WebMvcTest(ListaDeseosController.class)
@Import(SecurityConfig.class)
class ListaDeseosControllerTest {

    private static final String URL = "/api/customer/wishlist/items";
    private static final String URL_LISTA = "/api/customer/wishlist";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-04T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionListaDeseosUseCase gestionListaDeseos;

    @Test
    @DisplayName("HU14-CP07: añadir responde 200 con la lista; el cliente sale de la sesión y un clienteId en el cuerpo se ignora")
    void anadirRespondeConLaListaDelClienteEnSesion() throws Exception {
        when(gestionListaDeseos.anadir(CLIENTE, "p-1"))
                .thenReturn(new ListaDeseos("l-1", "c-1", List.of(new ItemDeseo("p-1", AHORA))));

        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": \"p-1\", \"clienteId\": \"otro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.items[0].addedAt").value("2026-11-04T10:00:00Z"));
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"{}", "{\"productoId\": null}", "{\"productoId\": \"\"}", "{\"productoId\": \" \"}",
            "{\"productoId\": "})
    @DisplayName("HU14-CP08: un cuerpo sin productoId, con productoId vacío o ilegible devuelve 400 y no llega al caso de uso")
    void cuerpoInvalidoNoLlegaAlCasoDeUso(String cuerpo) throws Exception {
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(cuerpo))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));

        verify(gestionListaDeseos, never()).anadir(any(), any());
    }

    @Test
    @DisplayName("HU14-CP09: un producto no disponible responde 404 con el código PRODUCT_NOT_FOUND")
    void unProductoNoDisponibleResponde404() throws Exception {
        when(gestionListaDeseos.anadir(CLIENTE, "p-9"))
                .thenThrow(new ProductoNoDisponibleException("El producto no está disponible"));

        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-9\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"))
                .andExpect(jsonPath("$.mensaje").value("El producto no existe"));
    }

    @Test
    @DisplayName("HU14-CP18: consultar responde 200 con los datos de cada producto, también de uno desactivado; el cliente sale de la sesión")
    void consultarRespondeConLosDatosDeCadaProducto() throws Exception {
        when(gestionListaDeseos.consultar(CLIENTE)).thenReturn(new ListaDeseosDetallada(List.of(
                new DeseoDetallado("p-1", "Teclado", "t.png", new BigDecimal("45.50"), 7, true, AHORA),
                new DeseoDetallado("p-3", "Webcam", "w.png", new BigDecimal("30.25"), 0, false, AHORA))));

        mockMvc.perform(get(URL_LISTA).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(2))
                .andExpect(jsonPath("$.items[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.items[0].nombre").value("Teclado"))
                .andExpect(jsonPath("$.items[0].imagen").value("t.png"))
                .andExpect(jsonPath("$.items[0].precioFinal").value(45.5))
                .andExpect(jsonPath("$.items[0].stock").value(7))
                .andExpect(jsonPath("$.items[0].disponible").value(true))
                .andExpect(jsonPath("$.items[0].addedAt").value("2026-11-04T10:00:00Z"))
                .andExpect(jsonPath("$.items[1].productoId").value("p-3"))
                .andExpect(jsonPath("$.items[1].disponible").value(false));
    }

    @Test
    @DisplayName("HU14-CP18: un cliente sin productos en la lista recibe 200 con items vacío")
    void consultarUnaListaVaciaRespondeConItemsVacio() throws Exception {
        when(gestionListaDeseos.consultar(CLIENTE)).thenReturn(new ListaDeseosDetallada(List.of()));

        mockMvc.perform(get(URL_LISTA).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(0));

        verify(gestionListaDeseos).consultar(CLIENTE);
    }

    @Test
    @DisplayName("HU14-CP29: quitar responde 200 con la lista actualizada; el cliente sale de la sesión")
    void quitarRespondeConLaListaActualizada() throws Exception {
        when(gestionListaDeseos.quitar(CLIENTE, "p-1")).thenReturn(new ListaDeseosDetallada(List.of(
                new DeseoDetallado("p-2", "Ratón", "r.png", new BigDecimal("20.50"), 3, true, AHORA))));

        mockMvc.perform(delete(URL + "/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].productoId").value("p-2"))
                .andExpect(jsonPath("$.items[0].nombre").value("Ratón"));
    }

    @Test
    @DisplayName("HU14-CP19: sin sesión no se consulta la lista de deseos")
    void sinSesionNoSeConsultaLaLista() throws Exception {
        mockMvc.perform(get(URL_LISTA))
                .andExpect(status().isUnauthorized());

        verify(gestionListaDeseos, never()).consultar(any());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"VENDEDOR", "ADMINISTRADOR"})
    @DisplayName("HU14-CP19: un usuario que no es cliente no consulta la lista de deseos")
    void noClienteNoConsultaLaLista(String rol) throws Exception {
        mockMvc.perform(get(URL_LISTA).with(user(CLIENTE).roles(rol)))
                .andExpect(status().isForbidden());

        verify(gestionListaDeseos, never()).consultar(any());
    }

    @Test
    @DisplayName("HU14-CP10: sin sesión no se añade a la lista de deseos")
    void sinSesionNoSeAnade() throws Exception {
        mockMvc.perform(post(URL).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\"}"))
                .andExpect(status().isUnauthorized());

        verify(gestionListaDeseos, never()).anadir(any(), any());
    }

    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"VENDEDOR", "ADMINISTRADOR"})
    @DisplayName("HU14-CP10: un usuario que no es cliente no añade a la lista de deseos")
    void noClienteNoAnade(String rol) throws Exception {
        mockMvc.perform(post(URL).with(user(CLIENTE).roles(rol)).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\"}"))
                .andExpect(status().isForbidden());

        verify(gestionListaDeseos, never()).anadir(any(), any());
    }

    @Test
    @DisplayName("HU14-CP10: una petición sin token CSRF se rechaza")
    void sinTokenCsrfNoSeAnade() throws Exception {
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\"}"))
                .andExpect(status().isForbidden());

        verify(gestionListaDeseos, never()).anadir(any(), any());
    }
}
