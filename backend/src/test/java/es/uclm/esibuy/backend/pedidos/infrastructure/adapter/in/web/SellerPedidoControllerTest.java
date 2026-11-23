package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import es.uclm.esibuy.backend.pedidos.application.port.in.GestionPedidosVendedorUseCase;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.config.SecurityConfig;

@WebMvcTest(SellerPedidoController.class)
@Import(SecurityConfig.class)
class SellerPedidoControllerTest {

    private static final String URL = "/api/seller/orders";
    private static final String VENDEDOR = "vendedor@esibuy.test";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GestionPedidosVendedorUseCase gestionPedidos;

    private static final Pedido PEDIDO = new Pedido(new DatosPedido(
            "o-1",
            new NumeroPedido("ESB-2026-000001"),
            "c-1",
            "u-2",
            "TecnoVera",
            EstadoPedido.PAGADO,
            List.of(new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91"))),
            new DireccionEnvioPedido("Carlos", "Calle Mayor 1", null, "13001", "Ciudad Real",
                    "Ciudad Real", "611222333"),
            "mp-1",
            Instant.parse("2026-10-29T10:00:00Z")));

    @Test
    @DisplayName("gestion-pedidos-vendedor: GET listado devuelve los pedidos reales del vendedor")
    void elListadoDevuelvePedidos() throws Exception {
        when(gestionPedidos.listarPedidos(VENDEDOR)).thenReturn(List.of(PEDIDO));

        mockMvc.perform(get(URL).with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value("o-1"))
                .andExpect(jsonPath("$[0].estado").value("PAGADO"));

        verify(gestionPedidos).listarPedidos(VENDEDOR);
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: GET detalle devuelve el pedido real")
    void elDetalleDevuelvePedido() throws Exception {
        when(gestionPedidos.consultarPedido(VENDEDOR, "o-1")).thenReturn(PEDIDO);

        mockMvc.perform(get(URL + "/o-1").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("o-1"))
                .andExpect(jsonPath("$.lineas[0].nombre").value("Auriculares"))
                .andExpect(jsonPath("$.direccionEnvio.calle").value("Calle Mayor 1"));
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: POST avance cambia y devuelve el estado")
    void elAvanceDevuelvePedido() throws Exception {
        Pedido preparado = new Pedido(new DatosPedido(
                "o-1",
                new NumeroPedido("ESB-2026-000001"),
                "c-1",
                "u-2",
                "TecnoVera",
                EstadoPedido.PREPARANDO,
                PEDIDO.getLineas(),
                PEDIDO.getDireccionEnvio(),
                "mp-1",
                PEDIDO.getCreatedAt()));
        when(gestionPedidos.avanzarEstado(VENDEDOR, "o-1")).thenReturn(preparado);

        mockMvc.perform(post(URL + "/o-1/advance").with(user(VENDEDOR).roles("VENDEDOR")).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("PREPARANDO"));
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: solo el vendedor accede y sin CSRF el avance se rechaza")
    void soloElVendedorAccede() throws Exception {
        mockMvc.perform(get(URL).with(user("cliente@esibuy.test").roles("CLIENTE")))
                .andExpect(status().isForbidden());
        mockMvc.perform(post(URL + "/o-1/advance").with(user(VENDEDOR).roles("VENDEDOR")))
                .andExpect(status().isForbidden());

        verifyNoInteractions(gestionPedidos);
    }
}
