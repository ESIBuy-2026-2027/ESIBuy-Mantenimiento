package es.uclm.esibuy.backend.carrito.application.port.in;

import java.math.BigDecimal;

/** Una línea del carrito con los datos actuales del producto: precio final del cliente, subtotal y stock. */
public record LineaCarritoDetallada(String productoId, String nombre, String imagen, int cantidad,
        BigDecimal precioUnitario, BigDecimal subtotal, int stock) {
}
