package es.uclm.esibuy.backend.alertas.application.port.in;

import java.math.BigDecimal;

/**
 * Cambio de un producto que puede interesar a los clientes que lo tienen en favoritos (AB#529): cómo estaba y cómo
 * queda. Lo envía el módulo de productos sin pasar su dominio.
 *
 * @param productoId id del producto
 * @param nombreProducto nombre del producto, para mostrar la alerta aunque el producto se elimine después
 * @param antes situación del producto antes del cambio
 * @param despues situación del producto después del cambio
 */
public record CambioProducto(String productoId, String nombreProducto, Situacion antes, Situacion despues) {

    /**
     * Lo que importa del producto para las alertas.
     *
     * @param precioEstandar precio que paga un cliente estándar (con el descuento general, si lo hay)
     * @param precioPremium precio que paga un cliente premium (con su descuento, si lo hay)
     * @param comprable si se puede comprar: disponible y con stock
     */
    public record Situacion(BigDecimal precioEstandar, BigDecimal precioPremium, boolean comprable) {
    }
}
