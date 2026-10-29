package es.uclm.esibuy.backend.carrito.application.port.out;

import java.math.BigDecimal;

/** Los datos de un producto que el carrito enseña y comprueba, con el precio final que paga el cliente. */
public record ProductoCarrito(String id, String vendedorId, String nombre, String imagen, BigDecimal precioFinal,
        int stock) {
}
