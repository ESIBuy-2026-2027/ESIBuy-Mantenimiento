package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

/**
 * Lo comercial de un producto que se puede comprar, para otros módulos como el carrito (HU9): así no ven el
 * dominio de productos. {@code precioFinal} ya lleva el descuento que le toca al cliente.
 */
public record ProductoComercial(String id, String vendedorId, String nombre, String imagen, BigDecimal precioFinal,
        int stock) {
}
