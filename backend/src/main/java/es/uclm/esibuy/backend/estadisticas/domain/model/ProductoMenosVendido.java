package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;

/** Un producto entre los menos vendidos del vendedor (HU17), con sus unidades vendidas e ingresos. */
public record ProductoMenosVendido(String productoId, String nombre, int unidades, BigDecimal ingresos, String imagen) {

    public ProductoMenosVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
        this(productoId, nombre, unidades, ingresos, "");
    }

    public ProductoMenosVendido {
        imagen = imagen == null ? "" : imagen;
    }
}
