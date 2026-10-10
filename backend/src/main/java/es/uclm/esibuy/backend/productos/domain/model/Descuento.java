package es.uclm.esibuy.backend.productos.domain.model;
import java.math.BigDecimal;

public record Descuento(boolean activo, BigDecimal porcentaje) {
    
        public static Descuento sinDescuento() {
            return null;
        }

    }
