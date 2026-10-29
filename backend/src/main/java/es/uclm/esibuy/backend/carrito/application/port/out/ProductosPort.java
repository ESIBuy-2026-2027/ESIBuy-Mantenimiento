package es.uclm.esibuy.backend.carrito.application.port.out;

import java.util.Optional;

/** Lo que el carrito necesita del módulo de productos: los datos actuales de un producto que se puede comprar. */
public interface ProductosPort {

    // Vacío si el producto no existe o no está disponible
    Optional<ProductoCarrito> consultarComprable(String productoId, boolean clientePremium);
}
