package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import jakarta.validation.constraints.AssertTrue;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;

/** Descuento tal y como viaja en la API, en la petición y en la respuesta del alta de producto (HU31). */
public record DescuentoDto(boolean activo, BigDecimal porcentaje) {

    // El formulario de vendedor admite porcentajes enteros del 1 al 99.
    // La regla se aplica en el DTO para no invalidar descuentos históricos ya almacenados.
    @AssertTrue(message = "El descuento debe ser un número entero entre 1 y 99")
    public boolean isPorcentajeValido() {
        return !activo || (porcentaje != null
                && porcentaje.stripTrailingZeros().scale() <= 0
                && porcentaje.compareTo(BigDecimal.ONE) >= 0
                && porcentaje.compareTo(new BigDecimal("99")) <= 0);
    }

    // Si no cumple las reglas generales, el constructor del dominio lanza ProductoInvalidoException.
    Descuento aDominio() {
        return new Descuento(activo, porcentaje);
    }

    static DescuentoDto desde(Descuento descuento) {
        return new DescuentoDto(descuento.activo(), descuento.porcentaje());
    }
}
