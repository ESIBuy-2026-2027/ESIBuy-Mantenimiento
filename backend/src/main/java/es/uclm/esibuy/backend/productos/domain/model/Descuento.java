package es.uclm.esibuy.backend.productos.domain.model;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;

public record Descuento(boolean activo, BigDecimal porcentaje) {

    private static final BigDecimal PORCENTAJE_MINIMO = new BigDecimal("1");
    private static final BigDecimal PORCENTAJE_MAXIMO = new BigDecimal("99");

    public Descuento {
        if (!activo) {
            porcentaje = null;
        } else if (porcentaje == null
                || porcentaje.stripTrailingZeros().scale() > 0
                || porcentaje.compareTo(PORCENTAJE_MINIMO) < 0
                || porcentaje.compareTo(PORCENTAJE_MAXIMO) > 0) {
            throw new ProductoInvalidoException("El descuento debe ser un número entero entre 1 y 99");
        }
    }

    public static Descuento sinDescuento() {
        return new Descuento(false, null);
    }
}
