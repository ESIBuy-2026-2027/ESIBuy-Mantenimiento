package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Fila del listado de productos del vendedor (HU32). No lleva vendedorId: ya es el de la sesión.
 *
 * <p>PENDIENTE: el contrato del frontend pide {@code categorias} con el nombre de la categoría; hasta que
 * exista el módulo de categorías se devuelve {@code categoriaId}.
 */
public record ProductoListadoResponse(
        String id,
        String nombre,
        BigDecimal precio,
        int stock,
        boolean disponible,
        String categoriaId
) {

    static ProductoListadoResponse desde(Producto producto) {
        return new ProductoListadoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getPrecio(),
                producto.getStock(),
                producto.isDisponible(),
                producto.getCategoriaId()
        );
    }
}
