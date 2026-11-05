package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;

/**
 * Una línea vendida de un pedido del vendedor que no se ha cancelado (HU17): el pedido al que pertenece, el producto
 * con su nombre en el momento de la compra, las unidades y lo que se cobró por ellas.
 */
public record VentaRegistrada(String pedidoId, String productoId, String nombreProducto, int unidades,
        BigDecimal importe) {
}
