package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;

/** Un producto entre los más vendidos del vendedor (HU17), con todas sus unidades vendidas y lo ingresado. */
public record ProductoMasVendido(String productoId, String nombre, int unidades, BigDecimal ingresos, String imagen) {

    public ProductoMasVendido(String productoId, String nombre, int unidades, BigDecimal ingresos) {
        this(productoId, nombre, unidades, ingresos, "");
    }

    public ProductoMasVendido {
        imagen = imagen == null ? "" : imagen;
    }
}
