package es.uclm.esibuy.backend.pedidos.application.port.out;

/** Lo que pedidos necesita del módulo de productos: descontar existencias y reponerlas si algo falla. */
public interface ExistenciasPort {

    // Devuelve false si no se puede comprar: puede faltar stock o estar el producto no disponible.
    boolean descontar(String productoId, int cantidad);

    void reponer(String productoId, int cantidad);
}
