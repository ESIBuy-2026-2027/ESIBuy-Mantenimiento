package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;

/**
 * Detalle de un producto del catálogo para el cliente (HU8), con su vendedor y el descuento que le corresponde.
 *
 * <p>PENDIENTE DE DECIDIR (equipo): {@code valoracion} y {@code numValoraciones} se conectarán con las
 * valoraciones de HU13; hasta entonces valen {@code null} y 0.
 */
public record DetalleProductoCatalogoResponse(
        String id,
        String nombre,
        String descripcion,
        String imagen,
        String categoriaId,
        Vendedor vendedor,
        BigDecimal precio,
        Descuento descuento,
        BigDecimal precioFinal,
        int stock,
        boolean disponible,
        Double valoracion,
        int numValoraciones
) {

    /** Vendedor del producto tal y como lo ve el cliente. */
    public record Vendedor(String id, String nombreComercial) {
    }

    /** Descuento que se aplica al cliente; si no tiene ninguno, el detalle lleva null. */
    public record Descuento(BigDecimal porcentaje) {
    }

    static DetalleProductoCatalogoResponse desde(DetalleProductoCatalogo detalle) {
        Descuento descuento = detalle.porcentajeDescuento() == null
                ? null
                : new Descuento(detalle.porcentajeDescuento());
        return new DetalleProductoCatalogoResponse(
                detalle.id(),
                detalle.nombre(),
                detalle.descripcion(),
                detalle.imagen(),
                detalle.categoriaId(),
                new Vendedor(detalle.vendedorId(), detalle.nombreComercial()),
                detalle.precio(),
                descuento,
                detalle.precioFinal(),
                detalle.stock(),
                detalle.disponible(),
                null,
                0
        );
    }
}