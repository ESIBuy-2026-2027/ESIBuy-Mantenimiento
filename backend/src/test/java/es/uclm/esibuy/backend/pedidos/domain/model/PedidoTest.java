package es.uclm.esibuy.backend.pedidos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;
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
        return new LineaPedido(productoId, "Producto " + productoId, cantidad, new BigDecimal(precio));
    }

    private static Pedido pedidoCon(List<LineaPedido> lineas) {
        return Pedido.nuevo(NUMERO, "c-1", "u-2", "TecnoVera", lineas, DIRECCION, "mp-1", AHORA);
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
    @DisplayName("HU10-CP12: un pedido sin cliente, proveedor, dirección o método de pago no es válido")
    void unPedidoSinDatosObligatoriosNoEsValido() {
        List<LineaPedido> lineas = List.of(linea("p-1", 1, "10.00"));

        assertThrows(PedidoInvalidoException.class,
                () -> Pedido.nuevo(NUMERO, " ", "u-2", "TecnoVera", lineas, DIRECCION, "mp-1", AHORA));
        assertThrows(PedidoInvalidoException.class,
                () -> Pedido.nuevo(NUMERO, "c-1", null, "TecnoVera", lineas, DIRECCION, "mp-1", AHORA));
        assertThrows(PedidoInvalidoException.class,
                () -> Pedido.nuevo(NUMERO, "c-1", "u-2", "TecnoVera", lineas, null, "mp-1", AHORA));
        assertThrows(PedidoInvalidoException.class,
                () -> Pedido.nuevo(NUMERO, "c-1", "u-2", "TecnoVera", lineas, DIRECCION, "", AHORA));
    }

    @Test
    @DisplayName("HU10-CP12: una línea necesita cantidad de al menos 1 y un precio que no sea negativo")
    void unaLineaNecesitaCantidadYPrecioValidos() {
        assertThrows(PedidoInvalidoException.class, () -> linea("p-1", 0, "10.00"));
        assertThrows(PedidoInvalidoException.class, () -> linea("p-1", 1, "-1.00"));
        assertThrows(PedidoInvalidoException.class, () -> new LineaPedido(" ", "Nombre", 1, BigDecimal.TEN));
    }

    @Test
    @DisplayName("HU10-CP12: un producto solo puede aparecer en una línea del pedido")
    void unProductoSoloApareceEnUnaLinea() {
        assertThrows(PedidoInvalidoException.class,
                () -> pedidoCon(List.of(linea("p-1", 1, "10.00"), linea("p-1", 2, "10.00"))));
    }
}
