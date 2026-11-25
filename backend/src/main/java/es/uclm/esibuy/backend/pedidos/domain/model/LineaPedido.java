package es.uclm.esibuy.backend.pedidos.domain.model;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoInvalidoException;

/**
 * Un producto del pedido con su vendedor, su nombre y su precio en el momento de la compra (precio histórico): si el
 * producto cambia después, el pedido no. El precio unitario ya lleva el descuento del producto aplicado.
 */
public record LineaPedido(
        String productoId,
        String nombreProducto,
        int cantidad,
        BigDecimal precioUnitario,
        String imagen) {

    public LineaPedido(String productoId, String nombreProducto, int cantidad, BigDecimal precioUnitario) {
        this(productoId, nombreProducto, cantidad, precioUnitario, null);
    }

    public LineaPedido {
        if (productoId == null || productoId.isBlank()) {
            throw new PedidoInvalidoException("La línea necesita el producto");
        }
        if (vendedorId == null || vendedorId.isBlank()) {
            throw new PedidoInvalidoException("La línea necesita el vendedor");
        }
        if (cantidad < 1) {
            throw new PedidoInvalidoException("La cantidad debe ser al menos 1");
        }
        if (precioUnitario == null || precioUnitario.signum() < 0) {
            throw new PedidoInvalidoException("El precio no puede ser negativo");
        }
        imagen = (imagen == null || imagen.isBlank()) ? null : imagen.trim();
    }

    public BigDecimal subtotal() {
        return precioUnitario.multiply(BigDecimal.valueOf(cantidad));
    }
}