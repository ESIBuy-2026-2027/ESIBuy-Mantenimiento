package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.pedidos.application.port.in.CancelacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.ConsultaComprasUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.application.port.in.SeguimientoEnvioUseCase;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoCancelableException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEntregableException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PagoRechazadoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficientePedidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.TotalNoCoincideException;
import es.uclm.esibuy.backend.pedidos.domain.model.CambioEstado;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
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
// Y la cancelación de un pedido (HU12, T374): POST /api/customer/orders/{id}/cancel, con CSRF.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU12-CP09 a CP12, propuestos.
// Y el seguimiento del envío: GET /api/customer/orders/{id}/tracking y POST .../confirm-delivery (HU11-CP28 a CP31).
@WebMvcTest(PedidoController.class)
@Import(SecurityConfig.class)
class PedidoControllerTest {

        private static final String URL = "/api/customer/orders";
        private static final String CLIENTE = "cliente@esibuy.test";
        private static final String CUERPO = "{\"vendedorId\": \"u-2\", \"direccionId\": \"d-1\", \"paymentMethodId\": \"mp-1\", \"totalEsperado\": 107.82, \"clienteId\": \"otro\"}";
        private static final RealizarPedidoCommand COMANDO = new RealizarPedidoCommand(CLIENTE, "u-2", "d-1", "mp-1",
                        new BigDecimal("107.82"));

        @Autowired
        private MockMvc mockMvc;

        @MockitoBean
        private RealizacionPedidoUseCase realizacionPedido;

        @MockitoBean
        private ConsultaComprasUseCase consultaCompras;

        @MockitoBean
        private CancelacionPedidoUseCase cancelacionPedido;

        @MockitoBean
        private SeguimientoEnvioUseCase seguimientoEnvio;

        private static final Pedido PEDIDO = new Pedido(
                        new DatosPedido(
                                        "o-1",
                                        new NumeroPedido("ESB-2026-000001"),
                                        "c-1",
                                        "u-2",
                                        "TecnoVera",
                                        EstadoPedido.PAGADO,
                                        List.of(new LineaPedido(
                                                        "p-1",
                                                        "u-2",
                                                        "Auriculares",
                                                        2,
                                                        new BigDecimal("53.91"))),
                                        new DireccionEnvioPedido(
                                                        "Carlos",
                                                        "Calle Mayor 1",
                                                        null,
                                                        "13001",
                                                        "Ciudad Real",
                                                        "Ciudad Real",
                                                        "611222333"),
                                        "mp-1",
                                        Instant.parse("2026-10-29T10:00:00Z")));

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

                realizar(CUERPO).andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
                realizar(CUERPO).andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"));
        }

        @Test
        @DisplayName("HU10-CP16: una petición sin proveedor, dirección o método de pago responde 400 sin llegar al caso de uso")
        void unaPeticionIncompletaResponde400() throws Exception {
                realizar("{\"vendedorId\": \"u-2\", \"direccionId\": \"d-1\"}").andExpect(status().isBadRequest());
                realizar("{\"vendedorId\": \"u-2\", \"paymentMethodId\": \"mp-1\"}").andExpect(status().isBadRequest());
                realizar("{\"direccionId\": \"d-1\", \"paymentMethodId\": \"mp-1\"}")
                                .andExpect(status().isBadRequest());
                realizar("{\"vendedorId\": \"u-2\", \"direccionId\": \"d-1\", \"paymentMethodId\": \"mp-1\"}")
                                .andExpect(status().isBadRequest());

                verify(realizacionPedido, never()).realizar(any());
        }

        @Test
        @DisplayName("HU10-CP15: si falta stock responde 409 con el code INSUFFICIENT_STOCK")
        void siFaltaStockResponde409() throws Exception {
                when(realizacionPedido.realizar(COMANDO)).thenThrow(new StockInsuficientePedidoException("no", "p-1", "Auriculares"));

                realizar(CUERPO).andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                                .andExpect(jsonPath("$.mensaje").value("No queda stock suficiente de «Auriculares»"));
        }

        @Test
        @DisplayName("HU10-CP26: si el pago se rechaza responde 402 con el code PAYMENT_REJECTED")
        void siElPagoSeRechazaResponde402() throws Exception {
                when(realizacionPedido.realizar(COMANDO)).thenThrow(new PagoRechazadoException("no"));

                realizar(CUERPO).andExpect(status().isPaymentRequired())
                                .andExpect(jsonPath("$.code").value("PAYMENT_REJECTED"))
                                .andExpect(jsonPath("$.mensaje").value("El pago ha sido rechazado"));
        }

        @Test
        @DisplayName("HU10-CP27: si el total que vio el cliente ya no es el real responde 409 con el code TOTAL_MISMATCH")
        void siElTotalNoCoincideResponde409() throws Exception {
                when(realizacionPedido.realizar(COMANDO)).thenThrow(new TotalNoCoincideException("no"));

                realizar(CUERPO).andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("TOTAL_MISMATCH"));
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

        // --- Historial y detalle de los pedidos (HU11)
        // -------------------------------------------------------------

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

        // --- Cancelación de un pedido (HU12)
        // -----------------------------------------------------------------------

        private static final Pedido CANCELADO = new Pedido(
                        new DatosPedido(
                                        "o-1",
                                        new NumeroPedido("ESB-2026-000001"),
                                        "c-1",
                                        "u-2",
                                        "TecnoVera",
                                        EstadoPedido.CANCELADO,
                                        PEDIDO.getLineas(),
                                        PEDIDO.getDireccionEnvio(),
                                        "mp-1",
                                        PEDIDO.getCreatedAt()));

        private ResultActions cancelar(String id) throws Exception {
                return mockMvc.perform(
                                post(URL + "/" + id + "/cancel").with(user(CLIENTE).roles("CLIENTE")).with(csrf()));
        }

        @Test
        @DisplayName("HU12-CP09: cancelar un pedido responde 200 con el pedido ya cancelado")
        void cancelarUnPedidoResponde200ConElPedidoCancelado() throws Exception {
                when(cancelacionPedido.cancelarPedido(CLIENTE, "o-1")).thenReturn(CANCELADO);

                cancelar("o-1")
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value("o-1"))
                                .andExpect(jsonPath("$.estado").value("CANCELADO"))
                                .andExpect(jsonPath("$.lineas[0].productoId").value("p-1"))
                                .andExpect(jsonPath("$.clienteId").doesNotExist());
        }

        @Test
        @DisplayName("HU12-CP10: un pedido que ya no se puede cancelar responde 409 con el code ORDER_NOT_CANCELLABLE")
        void unPedidoNoCancelableResponde409() throws Exception {
                when(cancelacionPedido.cancelarPedido(CLIENTE, "o-1")).thenThrow(new PedidoNoCancelableException("no"));

                cancelar("o-1")
                                .andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("ORDER_NOT_CANCELLABLE"))
                                .andExpect(jsonPath("$.mensaje").value("El pedido ya no se puede cancelar"));
        }

        @Test
        @DisplayName("HU12-CP11: cancelar un pedido que no existe o es de otro cliente responde 400 con el code ORDER_NOT_FOUND")
        void cancelarUnPedidoInexistenteOAjenoResponde400() throws Exception {
                when(cancelacionPedido.cancelarPedido(CLIENTE, "o-9")).thenThrow(new PedidoNoEncontradoException("no"));

                cancelar("o-9").andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        }

        @Test
        @DisplayName("HU12-CP12: sin sesión cancelar responde 401, con otro rol 403 y sin el token CSRF se rechaza")
        void soloElClienteCancelaSusPedidos() throws Exception {
                String url = URL + "/o-1/cancel";
                mockMvc.perform(post(url).with(csrf())).andExpect(status().isUnauthorized());
                mockMvc.perform(post(url).with(user("vendedor@esibuy.test").roles("VENDEDOR")).with(csrf()))
                                .andExpect(status().isForbidden());
                mockMvc.perform(post(url).with(user(CLIENTE).roles("CLIENTE"))).andExpect(status().isForbidden());

                verifyNoInteractions(cancelacionPedido);
        }

        // --- Seguimiento del envío (HU11-CP28 a CP31) ---

        private static final Instant ENTREGA = Instant.parse("2026-11-05T12:30:00Z");

        private static final Pedido ENVIADO = new Pedido(new DatosPedido("o-1", PEDIDO.getNumeroPedido(), "c-1", "u-2",
                        "TecnoVera", EstadoPedido.ENVIADO, PEDIDO.getLineas(), PEDIDO.getDireccionEnvio(), "mp-1",
                        PEDIDO.getCreatedAt(),
                        List.of(new CambioEstado(EstadoPedido.PAGADO, PEDIDO.getCreatedAt()),
                                        new CambioEstado(EstadoPedido.ENVIADO, Instant.parse("2026-10-30T09:00:00Z"))),
                        Instant.parse("2026-11-04T00:00:00Z"), null));

        private ResultActions seguimiento(String id) throws Exception {
                return mockMvc.perform(get(URL + "/" + id + "/tracking").with(user(CLIENTE).roles("CLIENTE")));
        }

        private ResultActions confirmarEntrega(String id) throws Exception {
                return mockMvc.perform(post(URL + "/" + id + "/confirm-delivery").with(user(CLIENTE).roles("CLIENTE"))
                                .with(csrf()));
        }

        @Test
        @DisplayName("HU11-CP28: el seguimiento responde 200 con el estado, la fecha estimada y el histórico del envío")
        void elSeguimientoResponde200ConElEstadoYElHistorico() throws Exception {
                when(seguimientoEnvio.consultarSeguimiento(CLIENTE, "o-1")).thenReturn(ENVIADO);

                seguimiento("o-1")
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.pedidoId").value("o-1"))
                                .andExpect(jsonPath("$.estado").value("ENVIADO"))
                                .andExpect(jsonPath("$.fechaEstimadaEntrega").value("2026-11-04T00:00:00Z"))
                                .andExpect(jsonPath("$.fechaEntrega").doesNotExist())
                                .andExpect(jsonPath("$.historial[0].estado").value("PAGADO"))
                                .andExpect(jsonPath("$.historial[1].estado").value("ENVIADO"))
                                .andExpect(jsonPath("$.historial[1].fecha").value("2026-10-30T09:00:00Z"))
                                .andExpect(jsonPath("$.clienteId").doesNotExist());
        }

        @Test
        @DisplayName("HU11-CP29: confirmar la entrega responde 200 con el seguimiento ya entregado")
        void confirmarLaEntregaResponde200ConElSeguimientoEntregado() throws Exception {
                Pedido entregado = new Pedido(new DatosPedido("o-1", PEDIDO.getNumeroPedido(), "c-1", "u-2", "TecnoVera",
                                EstadoPedido.ENTREGADO, PEDIDO.getLineas(), PEDIDO.getDireccionEnvio(), "mp-1",
                                PEDIDO.getCreatedAt(), List.of(new CambioEstado(EstadoPedido.ENTREGADO, ENTREGA)), null,
                                ENTREGA));
                when(seguimientoEnvio.confirmarEntrega(CLIENTE, "o-1")).thenReturn(entregado);

                confirmarEntrega("o-1")
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                                .andExpect(jsonPath("$.fechaEntrega").value("2026-11-05T12:30:00Z"))
                                .andExpect(jsonPath("$.historial[0].estado").value("ENTREGADO"));
        }

        @Test
        @DisplayName("HU11-CP30: un pedido que no está enviado responde 409 ORDER_NOT_DELIVERABLE; uno inexistente o ajeno, 400 ORDER_NOT_FOUND")
        void losErroresDelSeguimientoSonGenericos() throws Exception {
                when(seguimientoEnvio.confirmarEntrega(CLIENTE, "o-1")).thenThrow(new PedidoNoEntregableException("no"));
                when(seguimientoEnvio.confirmarEntrega(CLIENTE, "o-9")).thenThrow(new PedidoNoEncontradoException("no"));
                when(seguimientoEnvio.consultarSeguimiento(CLIENTE, "o-9")).thenThrow(new PedidoNoEncontradoException("no"));

                confirmarEntrega("o-1").andExpect(status().isConflict())
                                .andExpect(jsonPath("$.code").value("ORDER_NOT_DELIVERABLE"))
                                .andExpect(jsonPath("$.mensaje").value("El pedido no se puede marcar como entregado"));
                confirmarEntrega("o-9").andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
                seguimiento("o-9").andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
        }

        @Test
        @DisplayName("HU11-CP31: sin sesión el seguimiento y la confirmación responden 401, con otro rol 403, y confirmar sin CSRF se rechaza")
        void soloElClienteUsaElSeguimiento() throws Exception {
                String tracking = URL + "/o-1/tracking";
                String confirmar = URL + "/o-1/confirm-delivery";
                mockMvc.perform(get(tracking)).andExpect(status().isUnauthorized());
                mockMvc.perform(post(confirmar).with(csrf())).andExpect(status().isUnauthorized());
                mockMvc.perform(get(tracking).with(user("vendedor@esibuy.test").roles("VENDEDOR")))
                                .andExpect(status().isForbidden());
                mockMvc.perform(post(confirmar).with(user("vendedor@esibuy.test").roles("VENDEDOR")).with(csrf()))
                                .andExpect(status().isForbidden());
                mockMvc.perform(post(confirmar).with(user(CLIENTE).roles("CLIENTE"))).andExpect(status().isForbidden());

                verifyNoInteractions(seguimientoEnvio);
        }
}
