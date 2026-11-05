package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

/**
 * Detalle de un producto del catálogo para el cliente (HU8): así el adaptador web no ve el dominio de productos.
 * {@code porcentajeDescuento} es el del descuento que le toca al cliente, o {@code null} si no tiene ninguno, y
 * {@code precioFinal} ya lo lleva aplicado. {@code valoracion} es la media de las valoraciones (HU13), o
 * {@code null} si no tiene ninguna.
 */
public record DetalleProductoCatalogo(
        String id,
        String nombre,
        String descripcion,
        String imagen,
        String categoriaId,
        String vendedorId,
        String nombreComercial,
        BigDecimal precio,
        BigDecimal porcentajeDescuento,
        BigDecimal precioFinal,
        int stock,
        boolean disponible,
        Double valoracion,
        int numValoraciones
) {
}
