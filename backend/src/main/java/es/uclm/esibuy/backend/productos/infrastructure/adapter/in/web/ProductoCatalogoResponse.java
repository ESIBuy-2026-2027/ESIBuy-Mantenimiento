package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Producto del catálogo del cliente (HU7). No lleva precio, stock ni vendedorId.
 *
 * <p>PENDIENTE DE DECIDIR (equipo): {@code valoracion} y {@code numValoraciones} se conectarán con las
 * valoraciones de HU13; hasta entonces valen {@code null} y 0.
 */
public record ProductoCatalogoResponse(
        String id,
        String nombre,
        String imagen,
        String categoriaId,
        Double valoracion,
        int numValoraciones
) {

    static ProductoCatalogoResponse desde(Producto producto) {
        return new ProductoCatalogoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getImagen(),
                producto.getCategoriaId(),
                null,
                0
        );
    }
}
