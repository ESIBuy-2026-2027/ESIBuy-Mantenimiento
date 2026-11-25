package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.deseos.application.port.in.DeseoDetallado;
import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;
import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;
import es.uclm.esibuy.backend.deseos.domain.exception.ListaDeseosNoEncontradaException;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

@WebMvcTest(ListasDeseosController.class)
@Import(SecurityConfig.class)
class ListasDeseosControllerTest {

    private static final String URL = "/api/customer/wishlists";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-04T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionListaDeseosUseCase gestionListaDeseos;

    @Test
    @DisplayName("PBI534-CP04: consultar todas devuelve 200 con las listas del cliente")
    void consultarTodasDevuelve200() throws Exception {
        when(gestionListaDeseos.consultarTodas(CLIENTE)).thenReturn(List.of(
                new ListaDeseosDetallada("l-1", "Regalos", List.of(
                        new DeseoDetallado("p-1", "Teclado", "t.png", new BigDecimal("45.50"), 7, true, AHORA)))));

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("l-1"))
                .andExpect(jsonPath("$[0].nombre").value("Regalos"))
                .andExpect(jsonPath("$[0].items[0].productoId").value("p-1"));
    }

    @Test
    @DisplayName("PBI534-CP04: crear lista responde 201 con la lista creada")
    void crearListaResponde201() throws Exception {
        when(gestionListaDeseos.crearLista(CLIENTE, "Oficina"))
                .thenReturn(new ListaDeseosDetallada("l-2", "Oficina", List.of()));

        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"Oficina\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("l-2"))
                .andExpect(jsonPath("$.nombre").value("Oficina"));
    }

    @Test
    @DisplayName("PBI534-CP04: renombrar, añadir, quitar y eliminar lista funcionan y una inexistente responde 404")
    void editarYEliminarLista() throws Exception {
        when(gestionListaDeseos.renombrarLista(CLIENTE, "l-1", "Tech"))
                .thenReturn(new ListaDeseosDetallada("l-1", "Tech", List.of()));
        when(gestionListaDeseos.anadirALista(CLIENTE, "l-1", "p-1"))
                .thenReturn(new ListaDeseosDetallada("l-1", "Tech", List.of()));
        when(gestionListaDeseos.quitarDeLista(CLIENTE, "l-1", "p-1"))
                .thenReturn(new ListaDeseosDetallada("l-1", "Tech", List.of()));
        when(gestionListaDeseos.renombrarLista(CLIENTE, "no-existe", "X"))
                .thenThrow(new ListaDeseosNoEncontradaException());

        mockMvc.perform(patch(URL + "/l-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"Tech\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Tech"));

        mockMvc.perform(post(URL + "/l-1/items").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(delete(URL + "/l-1/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isOk());

        mockMvc.perform(delete(URL + "/l-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isNoContent());
        verify(gestionListaDeseos).eliminarLista(CLIENTE, "l-1");

        mockMvc.perform(patch(URL + "/no-existe").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\": \"X\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("WISHLIST_NOT_FOUND"));
    }
}
