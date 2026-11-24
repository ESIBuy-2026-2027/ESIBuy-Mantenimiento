package es.uclm.esibuy.backend.carrito.application.port.out;

import java.math.BigDecimal;

/** Los datos de un producto que el carrito enseña y comprueba, con el precio final que paga el cliente, el precio original y el
 * descuento que se le ha aplicado (nulo si no tiene). */
public record ProductoCarrito(String id, String vendedorId, String nombre, String imagen, BigDecimal precioFinal,
        int stock, BigDecimal precioOriginal, BigDecimal porcentajeDescuento) {
}
