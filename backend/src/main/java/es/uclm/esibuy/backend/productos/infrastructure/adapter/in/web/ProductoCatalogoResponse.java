package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

import java.math.BigDecimal;
/**
 * Producto del catálogo del cliente (HU7) con el precio que le corresponde (HU9): {@code descuento} es el que se le
 * aplica, o {@code null} si no tiene ninguno, y {@code precioFinal} ya lo lleva aplicado, como en el detalle (HU8).
 * Incluye stock y disponibilidad para que el cliente pueda añadir al carrito solo los productos comprables. No lleva
 * vendedorId. {@code valoracion} es la media de sus valoraciones (HU13), o {@code null} si no tiene ninguna.
 */
public record ProductoCatalogoResponse(
        String id,
        String nombre,
        String imagen,
        String categoriaId,
        BigDecimal precio,
        DetalleProductoCatalogoResponse.Descuento descuento,
        BigDecimal precioFinal,
        int stock,
        boolean disponible,
        Double valoracion,
        int numValoraciones,
        int stock,
        boolean disponible
) {
    static ProductoCatalogoResponse desde(Producto producto, boolean clientePremium) {
        var aplicado = producto.descuentoAplicado(clientePremium);
        return new ProductoCatalogoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getImagen(),
                producto.getCategoriaId(),
                producto.getPrecio(),
                aplicado.activo() ? new DetalleProductoCatalogoResponse.Descuento(aplicado.porcentaje()) : null,
                producto.precioFinal(clientePremium),
                producto.getStock(),
                producto.isDisponible(),
                producto.getValoracionMedia(),
                producto.getNumeroValoraciones(),
                producto.getStock(),
                producto.isDisponible()
        );
    }
}