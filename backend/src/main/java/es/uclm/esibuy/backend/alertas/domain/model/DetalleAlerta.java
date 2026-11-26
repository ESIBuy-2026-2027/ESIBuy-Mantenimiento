package es.uclm.esibuy.backend.alertas.domain.model;

import java.math.BigDecimal;

/**
 * Datos del producto en el momento de la alerta, para poder mostrarla aunque el producto cambie o se elimine (el
 * borrado de productos es físico). Todos son opcionales: los precios solo los lleva una bajada de precio.
 */
public record DetalleAlerta(String nombreProducto, String imagenProducto, BigDecimal precioAnterior,
        BigDecimal precioNuevo) {

    public static DetalleAlerta vacio() {
        return new DetalleAlerta(null, null, null, null);
    }
}
