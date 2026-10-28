package es.uclm.esibuy.backend.carrito.domain.model;

import java.time.Instant;

import es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException;

/**
 * Un producto del carrito y cuántas unidades quiere el cliente. Solo guarda el identificador del producto: el
 * precio, el nombre y el stock se consultan siempre al módulo de productos, para no enseñar datos viejos.
 */
public record LineaCarrito(String productoId, int cantidad, Instant addedAt, Instant updatedAt) {

    public LineaCarrito {
        exigirCantidad(cantidad);
    }

    static void exigirCantidad(int cantidad) {
        if (cantidad < 1) {
            throw new CantidadInvalidaException("La cantidad debe ser al menos 1");
        }
    }

    LineaCarrito conCantidad(int nuevaCantidad, Instant ahora) {
        return new LineaCarrito(productoId, nuevaCantidad, addedAt, ahora);
    }
}
