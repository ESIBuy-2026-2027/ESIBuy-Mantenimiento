package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/** Datos públicos mínimos de un producto destacado en la portada. */
public record ProductoDestacadoResponse(
        String id,
        String nombre,
        String imagen,
        Double valoracion
) {
    static ProductoDestacadoResponse desde(Producto producto) {
        return new ProductoDestacadoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getImagen(),
                producto.getValoracionMedia());
    }
}
