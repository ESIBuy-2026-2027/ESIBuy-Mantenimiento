package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;

/**
 * Datos para modificar un producto propio (HU34). El vendedor se identifica por el correo de la sesión; el id y el
 * vendedor del producto no se modifican.
 */
public record ModificarProductoCommand(
        String emailVendedor,
        String productoId,
        String nombre,
        String descripcion,
        BigDecimal precio,
        int stock,
        String categoriaId,
        String imagen,
        boolean disponible,
        Descuento descuento,
        Descuento descuentoPremium
) {
}
