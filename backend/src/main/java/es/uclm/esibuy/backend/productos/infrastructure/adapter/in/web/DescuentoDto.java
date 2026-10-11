package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;
import java.math.BigDecimal;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;

public record DescuentoDto (boolean activo, BigDecimal porcentaje) {

    Descuento aDominio() {
        return new Descuento(activo, porcentaje);
    }

    static DescuentoDto desde(Descuento descuento) {
        return new DescuentoDto(descuento.activo(), descuento.porcentaje());
    }
}
