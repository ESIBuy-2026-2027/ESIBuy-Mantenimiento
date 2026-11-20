package es.uclm.esibuy.backend.productos.domain.model;

import java.math.BigDecimal;

public record DatosProducto(
        DatosBasicos datosBasicos,
        Configuracion configuracion,
        Valoraciones valoraciones) {

    public record DatosBasicos(
            String vendedorId,
            String nombre,
            String descripcion,
            BigDecimal precio,
            int stock,
            String categoriaId,
            String imagen) {
    }

    public record Configuracion(
            boolean disponible,
            Descuento descuento,
            Descuento descuentoPremium) {
    }

    public record Valoraciones(
            Double valoracionMedia,
            int numeroValoraciones) {
    }
}