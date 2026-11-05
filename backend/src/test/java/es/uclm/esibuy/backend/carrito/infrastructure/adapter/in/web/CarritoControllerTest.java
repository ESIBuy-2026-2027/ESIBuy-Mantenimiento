package es.uclm.esibuy.backend.carrito.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.carrito.application.port.in.CarritoDetallado;
import es.uclm.esibuy.backend.carrito.application.port.in.GestionCarritoUseCase;
import es.uclm.esibuy.backend.carrito.application.port.in.GrupoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.in.LineaCarritoDetallada;
import es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException;
import es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException;
import es.uclm.esibuy.backend.carrito.domain.exception.ProductoNoComprableException;
import es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carrito del cliente (HU9): GET y DELETE sobre /api/customer/cart, y POST, PUT y DELETE de sus líneas en
// /items. El caso de uso es un doble de prueba; la seguridad es la real (SecurityConfig: /api/customer/** solo
// para CLIENTE, con CSRF).
@WebMvcTest(CarritoController.class)
@Import(SecurityConfig.class)
class CarritoControllerTest {

    private static final String URL = "/api/customer/cart";
    private static final String CLIENTE = "cliente@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionCarritoUseCase gestionCarrito;

    private static final CarritoDetallado CARRITO = new CarritoDetallado(List.of(new GrupoCarrito("u-2", "TecnoVera",
            new BigDecimal("107.82"), List.of(new LineaCarritoDetallada("p-1", "Auriculares", "a.png", 2,
                    new BigDecimal("53.91"), new BigDecimal("107.82"), 34)))), 2, new BigDecimal("107.82"));

    @Test
    @DisplayName("HU9-CP15: consultar devuelve el carrito con el contrato del frontend, agrupado por proveedor")
    void consultarDevuelveElCarritoAgrupadoPorProveedor() throws Exception {
        when(gestionCarrito.consultar(CLIENTE)).thenReturn(CARRITO);

        mockMvc.perform(get(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupos[0].vendedor.id").value("u-2"))
                .andExpect(jsonPath("$.grupos[0].vendedor.nombreComercial").value("TecnoVera"))
                .andExpect(jsonPath("$.grupos[0].total").value(107.82))
                .andExpect(jsonPath("$.grupos[0].lineas[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.grupos[0].lineas[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$.grupos[0].lineas[0].imagen").value("a.png"))
                .andExpect(jsonPath("$.grupos[0].lineas[0].cantidad").value(2))
                .andExpect(jsonPath("$.grupos[0].lineas[0].precioUnitario").value(53.91))
                .andExpect(jsonPath("$.grupos[0].lineas[0].subtotal").value(107.82))
                .andExpect(jsonPath("$.grupos[0].lineas[0].stock").value(34))
                .andExpect(jsonPath("$.totalProductos").value(2))
                .andExpect(jsonPath("$.total").value(107.82));
    }

    @Test
    @DisplayName("HU9-CP10: añadir unidades responde con el carrito completo")
    void anadirRespondeConElCarrito() throws Exception {
        when(gestionCarrito.anadir(CLIENTE, "p-1", 2)).thenReturn(CARRITO);

        mockMvc.perform(post(URL + "/items").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": \"p-1\", \"cantidad\": 2, \"clienteId\": \"otro\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(2));
    }

    @Test
    @DisplayName("HU9-CP11: superar el stock responde 409 con el code INSUFFICIENT_STOCK")
    void superarElStockResponde409() throws Exception {
        when(gestionCarrito.anadir(CLIENTE, "p-1", 50)).thenThrow(new StockInsuficienteException("sin stock"));
        when(gestionCarrito.fijarCantidad(CLIENTE, "p-1", 50)).thenThrow(new StockInsuficienteException("sin stock"));

        mockMvc.perform(post(URL + "/items").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\", \"cantidad\": 50}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
        mockMvc.perform(put(URL + "/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 50}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));
    }

    @Test
    @DisplayName("HU9-CP12: un producto que no existe o no está disponible responde 404")
    void unProductoQueNoSePuedeComprarResponde404() throws Exception {
        when(gestionCarrito.anadir(CLIENTE, "no-existe", 1)).thenThrow(new ProductoNoComprableException("no"));

        mockMvc.perform(post(URL + "/items").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"productoId\": \"no-existe\", \"cantidad\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU9-CP13: fijar la cantidad responde con el carrito; una cantidad menor que 1 responde 400")
    void fijarLaCantidadRespondeConElCarrito() throws Exception {
        when(gestionCarrito.fijarCantidad(CLIENTE, "p-1", 2)).thenReturn(CARRITO);
        when(gestionCarrito.fijarCantidad(CLIENTE, "p-1", 0)).thenThrow(new CantidadInvalidaException("no"));

        mockMvc.perform(put(URL + "/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 2}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupos[0].lineas[0].cantidad").value(2));
        mockMvc.perform(put(URL + "/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU9-CP13: fijar la cantidad de un producto que no está en el carrito responde 404")
    void fijarUnProductoQueNoEstaResponde404() throws Exception {
        when(gestionCarrito.fijarCantidad(CLIENTE, "p-9", 1)).thenThrow(new LineaCarritoNoEncontradaException("no"));

        mockMvc.perform(put(URL + "/items/p-9").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 1}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("CART_ITEM_NOT_FOUND"));
    }

    @Test
    @DisplayName("HU9-CP13: una petición sin producto o sin cantidad responde 400 sin llegar al caso de uso")
    void unaPeticionIncompletaResponde400() throws Exception {
        mockMvc.perform(post(URL + "/items").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"cantidad\": 1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put(URL + "/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());

        verify(gestionCarrito, never()).anadir(anyString(), anyString(), anyInt());
        verify(gestionCarrito, never()).fijarCantidad(anyString(), anyString(), anyInt());
    }

    @Test
    @DisplayName("HU9-CP14: quitar un producto y vaciar el carrito responden con el carrito")
    void quitarYVaciarRespondenConElCarrito() throws Exception {
        CarritoDetallado vacio = new CarritoDetallado(List.of(), 0, BigDecimal.ZERO);
        when(gestionCarrito.quitar(CLIENTE, "p-1")).thenReturn(vacio);
        when(gestionCarrito.vaciar(CLIENTE)).thenReturn(vacio);

        mockMvc.perform(delete(URL + "/items/p-1").with(user(CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.grupos").isEmpty());
        mockMvc.perform(delete(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProductos").value(0));
    }

    @Test
    @DisplayName("HU9-CP16: sin sesión responde 401 y un vendedor o un administrador, 403")
    void soloElClienteUsaElCarrito() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(URL).with(user("vendedor@esibuy.test").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(URL + "/items").with(user("admin@esibuy.test").roles("ADMINISTRADOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"productoId\": \"p-1\", \"cantidad\": 1}"))
                .andExpect(status().isForbidden());

        verify(gestionCarrito, never()).consultar(any());
    }

    @Test
    @DisplayName("HU9-CP16: cambiar el carrito sin el token CSRF se rechaza")
    void cambiarElCarritoSinCsrfSeRechaza() throws Exception {
        mockMvc.perform(delete(URL).with(user(CLIENTE).roles("CLIENTE")))
                .andExpect(status().isForbidden());

        verify(gestionCarrito, never()).vaciar(any());
    }
}
