package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;

/** Datos para dar de alta un producto. El vendedor se identifica por el correo de la sesión. */
public record CrearProductoCommand(
        String emailVendedor,
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