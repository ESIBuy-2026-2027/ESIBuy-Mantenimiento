package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

public record ProductoResponse(
        String id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        int stock,
        String categoriaId,
        String imagen,
        boolean disponible,
        DescuentoDto descuento,
        DescuentoDto descuentoPremium
) {

    static ProductoResponse desde(Producto producto) {
        return new ProductoResponse(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getStock(),
                producto.getCategoriaId(),
                producto.getImagen(),
                producto.isDisponible(),
                DescuentoDto.desde(producto.getDescuento()),
                DescuentoDto.desde(producto.getDescuentoPremium())
        );
    }
}
