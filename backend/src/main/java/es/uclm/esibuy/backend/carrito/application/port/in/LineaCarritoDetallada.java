package es.uclm.esibuy.backend.carrito.application.port.in;

import java.math.BigDecimal;

/**
 * Una línea del carrito con los datos actuales del producto: precio final del cliente (el descuento va en el precio
 * de cada producto), subtotal y stock, y para enseñarlo, el precio original y el porcentaje de descuento aplicado
 * (nulo si el producto no tiene descuento).
 */
public record LineaCarritoDetallada(String productoId, String nombre, String imagen, int cantidad,
        BigDecimal precioUnitario, BigDecimal subtotal, int stock, BigDecimal precioOriginal,
        BigDecimal porcentajeDescuento) {
}
