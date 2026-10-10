package es.uclm.esibuy.backend.productos.domain.model;
import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;

public record Descuento(boolean activo, BigDecimal porcentaje) {

    private static final BigDecimal PORCENTAJE_MAXIMO = new BigDecimal("100");
    public Descuento {
        if (!activo) {
            porcentaje = null;
        } else {
            if (porcentaje == null || porcentaje.signum() <= 0 || porcentaje.compareTo(PORCENTAJE_MAXIMO) > 0) {
                throw new ProductoInvalidoException("El descuento debe ser mayor que 0 y no superar 100");            
            }
        }
    }

    public static Descuento sinDescuento() {
        return new Descuento(false, null);
    }

}
