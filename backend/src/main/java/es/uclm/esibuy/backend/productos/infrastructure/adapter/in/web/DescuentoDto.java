package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;

/** Descuento tal y como viaja en la API, en la petición y en la respuesta del alta de producto (HU31). */
public record DescuentoDto(boolean activo, BigDecimal porcentaje) {

    // Si no cumple las reglas, el constructor del dominio lanza ProductoInvalidoException.
    Descuento aDominio() {
        return new Descuento(activo, porcentaje);
    }

    static DescuentoDto desde(Descuento descuento) {
        return new DescuentoDto(descuento.activo(), descuento.porcentaje());
    }
}
