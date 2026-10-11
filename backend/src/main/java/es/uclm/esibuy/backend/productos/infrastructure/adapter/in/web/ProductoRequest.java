package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.math.BigDecimal;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;

/** Cuerpo del alta de producto (HU31). No lleva vendedor: sale de la sesión. */
public record ProductoRequest(
        @NotBlank @Size(max = 100) String nombre,
        @Size(max = 1000) String descripcion,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal precio,
        @NotNull @Min(0) Integer stock,
        @NotBlank String categoriaId,
        @Pattern(regexp = "(?i)(https?://\\S+)?") String imagen,
        boolean disponible,
        @NotNull @Valid DescuentoDto descuento,
        @NotNull @Valid DescuentoDto descuentoPremium
) {

    CrearProductoCommand aComando(String emailVendedor) {
        return new CrearProductoCommand(emailVendedor, nombre, descripcion, precio, stock, categoriaId, imagen,
                disponible, descuento.aDominio(), descuentoPremium.aDominio());
    }
}
