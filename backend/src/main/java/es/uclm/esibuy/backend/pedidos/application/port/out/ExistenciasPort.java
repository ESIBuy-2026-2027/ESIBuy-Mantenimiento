package es.uclm.esibuy.backend.pedidos.application.port.out;

/** Lo que pedidos necesita del módulo de productos: descontar el stock que se compra y reponerlo si algo falla. */
public interface ExistenciasPort {

    // Si no hay stock suficiente devuelve false y no descuenta nada
    boolean descontar(String productoId, int cantidad);

    void reponer(String productoId, int cantidad);
}
