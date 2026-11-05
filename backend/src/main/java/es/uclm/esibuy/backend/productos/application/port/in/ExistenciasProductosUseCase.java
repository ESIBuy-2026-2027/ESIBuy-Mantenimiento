package es.uclm.esibuy.backend.productos.application.port.in;

/**
 * Stock de los productos cuando se compran (HU10): el módulo de pedidos lo descuenta al crear un pedido y lo
 * repone si el pedido no llega a crearse. No hay un caso de uso del vendedor ni del catálogo con este propósito.
 */
public interface ExistenciasProductosUseCase {

    /**
     * Quita {@code cantidad} unidades del stock en una sola operación, solo si hay bastantes.
     *
     * @return si se han descontado; si no había stock suficiente, el stock no cambia
     * @throws es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException si la cantidad es menor que 1
     */
    boolean descontar(String productoId, int cantidad);

    /** Devuelve al stock unas unidades descontadas antes, cuando el pedido no llega a crearse. */
    void reponer(String productoId, int cantidad);
}
