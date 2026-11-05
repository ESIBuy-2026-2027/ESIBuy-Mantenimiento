package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficientePedidoException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Realización de pedidos (HU10): POST /api/customer/orders. El caso de uso es un doble de prueba; la seguridad es
// la real (SecurityConfig: /api/customer/** solo para CLIENTE, con CSRF).
// También el historial y el detalle de los pedidos del cliente (HU11, T377): GET /api/customer/orders y
// GET /api/customer/orders/{id}. PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU11-CP07 a CP10, propuestos.
@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

    private static final String URL = "/api/customer/orders";
    private static final String CLIENTE = "cliente@esibuy.test";
    private static final String CUERPO =
            "{\"vendedorId\": \"u-2\", \"direccionId\": \"d-1\", \"paymentMethodId\": \"mp-1\", \"clienteId\": \"otro\"}";
    private static final RealizarPedidoCommand COMANDO = new RealizarPedidoCommand(CLIENTE, "u-2", "d-1", "mp-1");

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RealizacionPedidoUseCase realizacionPedido;

    @MockitoBean
    private ConsultaComprasUseCase consultaCompras;

    private static final Pedido PEDIDO = new Pedido("o-1", new NumeroPedido("ESB-2026-000001"), "c-1", "u-2",
            "TecnoVera", EstadoPedido.PAGADO, List.of(new LineaPedido("p-1", "Auriculares", 2,
                    new BigDecimal("53.91"))),
            new DireccionEnvioPedido("Carlos", "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real",
                    "611222333"),
            "mp-1", Instant.parse("2026-10-29T10:00:00Z"));

    private ResultActions realizar(String cuerpo) throws Exception {
        return mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE")).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(cuerpo));
    }

    @Test
    @DisplayName("HU10-CP14: realizar un pedido responde 201 con el pedido con el contrato del frontend")
    void realizarUnPedidoResponde201ConElPedido() throws Exception {
        when(realizacionPedido.realizar(COMANDO)).thenReturn(PEDIDO);

        realizar(CUERPO)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("o-1"))
                .andExpect(jsonPath("$.numeroPedido").value("ESB-2026-000001"))
                .andExpect(jsonPath("$.vendedor.id").value("u-2"))
                .andExpect(jsonPath("$.vendedor.nombreComercial").value("TecnoVera"))
                .andExpect(jsonPath("$.total").value(107.82))
                .andExpect(jsonPath("$.estado").value("PAGADO"))
                .andExpect(jsonPath("$.paymentMethodId").value("mp-1"))
                .andExpect(jsonPath("$.direccionEnvio.calle").value("Calle Mayor 1"))
                .andExpect(jsonPath("$.lineas[0].productoId").value("p-1"))
                .andExpect(jsonPath("$.lineas[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$.lineas[0].cantidad").value(2))
                .andExpect(jsonPath("$.lineas[0].precioUnitario").value(53.91))
                .andExpect(jsonPath("$.lineas[0].subtotal").value(107.82))
                .andExpect(jsonPath("$.createdAt").value("2026-10-29T10:00:00Z"))
                .andExpect(jsonPath("$.clienteId").doesNotExist());
    }

    @Test
    @DisplayName("HU10-CP16: un método de pago o una dirección que no son del cliente responden 400")
    void unMetodoODireccionAjenosResponden400() throws Exception {
        when(realizacionPedido.realizar(COMANDO))
                .thenThrow(new MetodoPagoNoValidoException("no"), new DireccionNoValidaException("no"));

        realizar(CUERPO).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        realizar(CUERPO).andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
    }

    @Test
    @DisplayName("HU10-CP16: una petición sin proveedor, dirección o método de pago responde 400 sin llegar al caso de uso")
    void unaPeticionIncompletaResponde400() throws Exception {
        realizar("{\"vendedorId\": \"u-2\", \"direccionId\": \"d-1\"}").andExpect(status().isBadRequest());
        realizar("{\"vendedorId\": \"u-2\", \"paymentMethodId\": \"mp-1\"}").andExpect(status().isBadRequest());
        realizar("{\"direccionId\": \"d-1\", \"paymentMethodId\": \"mp-1\"}").andExpect(status().isBadRequest());

        verify(realizacionPedido, never()).realizar(any());
    }

    @Test
    @DisplayName("HU10-CP15: si falta stock responde 409 con el code INSUFFICIENT_STOCK")
    void siFaltaStockResponde409() throws Exception {
        when(realizacionPedido.realizar(COMANDO)).thenThrow(new StockInsuficientePedidoException("no"));

        realizar(CUERPO).andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.mensaje").value("No se puede comprar un producto del pedido"));
    }

    @Test
    @DisplayName("HU10-CP17: sin productos de ese proveedor en el carrito responde 409 con el code CART_EMPTY")
    void sinProductosDelProveedorResponde409() throws Exception {
        when(realizacionPedido.realizar(COMANDO)).thenThrow(new CarritoSinProductosDelProveedorException("no"));

        realizar(CUERPO).andExpect(status().isConflict()).andExpect(jsonPath("$.code").value("CART_EMPTY"));
    }

    @Test
    @DisplayName("HU10-CP18: sin sesión responde 401, otro rol 403 y sin CSRF se rechaza")
    void soloElClienteRealizaPedidos() throws Exception {
        mockMvc.perform(post(URL).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(URL).with(user("vendedor@esibuy.test").roles("VENDEDOR")).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(URL).with(user(CLIENTE).roles("CLIENTE"))
                        .contentType(MediaType.APPLICATION_JSON).content(CUERPO))
                .andExpect(status().isForbidden());

        verify(realizacionPedido, never()).realizar(any());
    }

    // --- Historial y detalle de los pedidos (HU11) -------------------------------------------------------------

    private ResultActions consultar(String url) throws Exception {
        return mockMvc.perform(get(url).with(user(CLIENTE).roles("CLIENTE")));
    }

    @Test
    @DisplayName("HU11-CP07: el historial responde 200 con los pedidos del cliente de la sesión, cada uno completo")
    void elHistorialResponde200ConLosPedidosDelCliente() throws Exception {
        when(consultaCompras.listarPedidos(CLIENTE)).thenReturn(List.of(PEDIDO));

        consultar(URL)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("o-1"))
                .andExpect(jsonPath("$[0].numeroPedido").value("ESB-2026-000001"))
                .andExpect(jsonPath("$[0].vendedor.nombreComercial").value("TecnoVera"))
                .andExpect(jsonPath("$[0].estado").value("PAGADO"))
                .andExpect(jsonPath("$[0].total").value(107.82))
                .andExpect(jsonPath("$[0].lineas[0].precioUnitario").value(53.91))
                .andExpect(jsonPath("$[0].lineas[0].subtotal").value(107.82))
                .andExpect(jsonPath("$[0].createdAt").value("2026-10-29T10:00:00Z"))
                .andExpect(jsonPath("$[0].clienteId").doesNotExist());
    }

    @Test
    @DisplayName("HU11-CP07: un cliente sin pedidos recibe un historial vacío")
    void unClienteSinPedidosRecibeUnaListaVacia() throws Exception {
        when(consultaCompras.listarPedidos(CLIENTE)).thenReturn(List.of());

        consultar(URL).andExpect(status().isOk()).andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @DisplayName("HU11-CP08: el detalle responde 200 con el pedido: productos, desglose de precios, estado y envío")
    void elDetalleResponde200ConElPedido() throws Exception {
        when(consultaCompras.consultarPedido(CLIENTE, "o-1")).thenReturn(PEDIDO);

        consultar(URL + "/o-1")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o-1"))
                .andExpect(jsonPath("$.estado").value("PAGADO"))
                .andExpect(jsonPath("$.total").value(107.82))
                .andExpect(jsonPath("$.lineas[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$.lineas[0].cantidad").value(2))
                .andExpect(jsonPath("$.direccionEnvio.calle").value("Calle Mayor 1"));
    }

    @Test
    @DisplayName("HU11-CP09: un pedido que no existe o es de otro cliente responde 400 con el code ORDER_NOT_FOUND")
    void unPedidoInexistenteOAjenoResponde400() throws Exception {
        when(consultaCompras.consultarPedido(CLIENTE, "o-9")).thenThrow(new PedidoNoEncontradoException("no"));

        consultar(URL + "/o-9")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"))
                .andExpect(jsonPath("$.mensaje").value("El pedido no existe"));
    }

    @Test
    @DisplayName("HU11-CP10: sin sesión el historial y el detalle responden 401, y con otro rol 403")
    void soloElClienteConsultaSusPedidos() throws Exception {
        mockMvc.perform(get(URL)).andExpect(status().isUnauthorized());
        mockMvc.perform(get(URL + "/o-1")).andExpect(status().isUnauthorized());
        mockMvc.perform(get(URL).with(user("vendedor@esibuy.test").roles("VENDEDOR")))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(URL + "/o-1").with(user("admin@esibuy.test").roles("ADMINISTRADOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(consultaCompras);
    }
}
