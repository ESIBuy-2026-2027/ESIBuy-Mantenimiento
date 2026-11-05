package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Producto del catálogo del cliente (HU7). No lleva precio, stock ni vendedorId. {@code valoracion} es la media de
 * sus valoraciones (HU13), o {@code null} si no tiene ninguna.
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
                producto.getValoracionMedia(),
                producto.getNumeroValoraciones()
        );
    }
}
