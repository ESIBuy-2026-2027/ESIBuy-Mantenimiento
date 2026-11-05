package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.math.BigDecimal;

/**
 * Lo que otros módulos ven de una línea vendida (HU17): el pedido, el producto con su nombre en el momento de la
 * compra, las unidades y lo que se cobró por ellas. Así no ven el dominio de pedidos.
 */
public record VentaProducto(String pedidoId, String productoId, String nombreProducto, int unidades,
        BigDecimal importe) {
}
