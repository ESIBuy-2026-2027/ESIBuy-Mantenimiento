package es.uclm.esibuy.backend.deseos.application.port.out;

/** Lo que la lista de deseos necesita del módulo de productos: saber si un producto se puede añadir. */
public interface ProductosPort {

    // Falso si el producto no existe o no está disponible
    boolean estaDisponible(String productoId);
}
