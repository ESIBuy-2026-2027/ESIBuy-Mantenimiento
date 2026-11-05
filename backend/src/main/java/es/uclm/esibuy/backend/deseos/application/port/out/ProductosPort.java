package es.uclm.esibuy.backend.deseos.application.port.out;

import java.util.Optional;

/** Lo que la lista de deseos necesita del módulo de productos: si un producto se puede añadir y sus datos actuales. */
public interface ProductosPort {

    // Falso si el producto no existe o no está disponible
    boolean estaDisponible(String productoId);

    // Vacío solo si el producto no existe: uno desactivado también tiene datos
    Optional<ProductoDeseado> consultarResumen(String productoId, boolean clientePremium);
}
