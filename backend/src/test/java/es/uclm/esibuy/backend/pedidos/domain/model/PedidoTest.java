package es.uclm.esibuy.backend.pedidos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoCancelableException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEntregableException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class PedidoTest {

    private static final Instant AHORA = Instant.parse("2026-10-29T10:00:00Z");
    private static final DireccionEnvioPedido DIRECCION = new DireccionEnvioPedido("Carlos Cliente",
            "Calle Mayor 1", "2º B", "13001", "Ciudad Real", "Ciudad Real", "611222333");
    private static final NumeroPedido NUMERO = NumeroPedido.de(2026, 1);

    private static LineaPedido linea(String productoId, int cantidad, String precio) {
        return linea("u-2", productoId, cantidad, precio);
    }

    private static LineaPedido linea(String vendedorId, String productoId, int cantidad, String precio) {
        return new LineaPedido(productoId, vendedorId, "Producto " + productoId, cantidad, new BigDecimal(precio));
    }

    private static Pedido pedidoCon(List<LineaPedido> lineas) {
        DatosPedido datosPedido = new DatosPedido(
                null,
                NUMERO,
                "c-1",
                "u-2",
                "TecnoVera",
                EstadoPedido.PAGADO,
                lineas,
                DIRECCION,
                "mp-1",
                AHORA);

        return Pedido.nuevo(datosPedido);
    }

    @Test
    @DisplayName("HU10-CP12: un pedido nuevo nace pagado, con su número y el total de sus líneas")
    void unPedidoNuevoNacePagadoConSuTotal() {
        Pedido pedido = pedidoCon(List.of(linea("p-1", 2, "53.91"), linea("p-2", 1, "89.50")));

        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
        assertEquals("ESB-2026-000001", pedido.getNumeroPedido().valor());
        assertEquals(new BigDecimal("107.82"), pedido.getLineas().get(0).subtotal());
        assertEquals(new BigDecimal("197.32"), pedido.getTotal());
        assertEquals("u-2", pedido.getVendedorId());
        assertEquals("TecnoVera", pedido.getNombreComercial());
        assertEquals(DIRECCION, pedido.getDireccionEnvio());
        assertEquals("mp-1", pedido.getPaymentMethodId());
        assertEquals(AHORA, pedido.getCreatedAt());
    }

    @Test
    @DisplayName("HU10-CP12: un pedido sin líneas no es válido")
    void unPedidoSinLineasNoEsValido() {
        assertThrows(PedidoInvalidoException.class, () -> pedidoCon(List.of()));
    }

    @Test
    @DisplayName("HU10-CP12: una línea necesita cantidad de al menos 1 y un precio que no sea negativo")
    void unaLineaNecesitaCantidadYPrecioValidos() {
        assertThrows(PedidoInvalidoException.class, () -> linea("p-1", 0, "10.00"));
        assertThrows(PedidoInvalidoException.class, () -> linea("p-1", 1, "-1.00"));
        assertThrows(PedidoInvalidoException.class, () -> new LineaPedido(" ", "u-2", "Nombre", 1, BigDecimal.TEN));
    }

    @Test
    @DisplayName("HU10-CP24: un pedido no admite productos de más de un vendedor")
    void unPedidoNoAdmiteProductosDeOtroVendedor() {
        List<LineaPedido> lineasMezcladas = List.of(
                linea("u-2", "p-1", 1, "10.00"),
                linea("u-99", "p-24", 1, "129.00"));

        assertThrows(PedidoInvalidoException.class, () -> pedidoCon(lineasMezcladas));
    }

    @Test
    @DisplayName("HU10-CP24: una línea necesita el vendedor del producto")
    void unaLineaNecesitaSuVendedor() {
        assertThrows(PedidoInvalidoException.class, () -> new LineaPedido("p-1", " ", "Nombre", 1, BigDecimal.TEN));
        assertThrows(PedidoInvalidoException.class, () -> new LineaPedido("p-1", null, "Nombre", 1, BigDecimal.TEN));
    }

    @Test
    @DisplayName("HU10-CP25: el total es la suma de las líneas, cada una con el descuento ya aplicado a su precio")
    void elTotalEsLaSumaDeLasLineasConSuDescuento() {
        // 59.90 con un 10 % de descuento en el producto son 53.91; el descuento no se aplica al total
        List<LineaPedido> lineas = List.of(linea("p-1", 2, "53.91"), linea("p-2", 1, "89.50"));

        assertEquals(new BigDecimal("197.32"), Pedido.totalDe(lineas));
        assertEquals(Pedido.totalDe(lineas), pedidoCon(lineas).getTotal());
    }

    @Test
    @DisplayName("HU10-CP12: un producto solo puede aparecer en una línea del pedido")
    void unProductoSoloApareceEnUnaLinea() {
        List<LineaPedido> lineasDuplicadas = List.of(
                linea("p-1", 1, "10.00"),
                linea("p-1", 2, "10.00"));

        assertThrows(PedidoInvalidoException.class,
                () -> pedidoCon(lineasDuplicadas));
    }

    // --- Cancelación (HU12)
    // ------------------------------------------------------------------------------------
    // Criterio de aceptación: solo se cancela si el pedido aún no se ha enviado.
    // PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU12-CP01 y CP02, propuestos.

    private static Pedido pedidoEn(EstadoPedido estado) {
        DatosPedido datosPedido = new DatosPedido(
                "o-1",
                NUMERO,
                "c-1",
                "u-2",
                "TecnoVera",
                estado,
                List.of(linea("p-1", 1, "10.00")),
                DIRECCION,
                "mp-1",
                AHORA);

        return new Pedido(datosPedido);
    }

    @Test
    @DisplayName("HU12-CP01: un pedido pagado o en preparación, que aún no se ha enviado, se puede cancelar")
    void unPedidoNoEnviadoSeCancela() {
        for (EstadoPedido estado : List.of(EstadoPedido.PAGADO, EstadoPedido.PREPARANDO)) {
            Pedido pedido = pedidoEn(estado);

            pedido.cancelar();

            assertEquals(EstadoPedido.CANCELADO, pedido.getEstado());
        }
    }

    @Test
    @DisplayName("HU12-CP02: un pedido enviado, entregado o ya cancelado no se puede cancelar y no cambia")
    void unPedidoEnviadoEntregadoOCanceladoNoSeCancela() {
        for (EstadoPedido estado : List.of(EstadoPedido.ENVIADO, EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)) {
            Pedido pedido = pedidoEn(estado);

            assertThrows(PedidoNoCancelableException.class, pedido::cancelar);
            assertEquals(estado, pedido.getEstado());
        }
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: el estado avanza linealmente sin retroceder")
    void elEstadoAvanzaLinealmente() {
        Pedido pedido = pedidoEn(EstadoPedido.PAGADO);

        pedido.avanzarEstado();
        assertEquals(EstadoPedido.PREPARANDO, pedido.getEstado());
        pedido.avanzarEstado();
        assertEquals(EstadoPedido.ENVIADO, pedido.getEstado());
        pedido.avanzarEstado();
        assertEquals(EstadoPedido.ENTREGADO, pedido.getEstado());
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: no se avanza un pedido entregado o cancelado")
    void noSeAvanzaUnPedidoFinal() {
        for (EstadoPedido estado : List.of(EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO)) {
            Pedido pedido = pedidoEn(estado);

            assertThrows(es.uclm.esibuy.backend.pedidos.domain.exception.PedidoEstadoNoAvanzableException.class,
                    pedido::avanzarEstado);
            assertEquals(estado, pedido.getEstado());
        }
    }
}
