package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

/**
 * Lo que otros módulos enseñan de un producto aunque ya no se pueda comprar, como la lista de deseos (HU14): así no
 * ven el dominio de productos. {@code precioFinal} ya lleva el descuento que le toca al cliente.
 */
public record ResumenProducto(String id, String nombre, String imagen, BigDecimal precioFinal, int stock,
        boolean disponible) {
}
