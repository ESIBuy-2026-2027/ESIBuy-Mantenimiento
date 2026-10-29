package es.uclm.esibuy.backend.carrito.application.port.in;

/**
 * Gestión de su carrito por parte del cliente (HU9). Recibe el correo del cliente en sesión: solo puede ver y
 * cambiar su carrito. Todas las operaciones devuelven el carrito completo, como espera el frontend.
 */
public interface GestionCarritoUseCase {

    /** El carrito del cliente, ya limpio de lo que no se puede comprar. Si no tiene, uno vacío. */
    CarritoDetallado consultar(String emailCliente);

    /**
     * Suma unidades de un producto a las que ya hubiera.
     *
     * @throws es.uclm.esibuy.backend.carrito.domain.exception.ProductoNoComprableException si no existe o no
     *         está disponible
     * @throws es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException si se pasa del stock
     * @throws es.uclm.esibuy.backend.carrito.domain.exception.CantidadInvalidaException si es menor que 1
     */
    CarritoDetallado anadir(String emailCliente, String productoId, int cantidad);

    /**
     * Sustituye la cantidad de un producto del carrito. Mismos errores que al añadir, y además:
     *
     * @throws es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException si el
     *         producto no está en el carrito
     */
    CarritoDetallado fijarCantidad(String emailCliente, String productoId, int cantidad);

    CarritoDetallado quitar(String emailCliente, String productoId);

    CarritoDetallado vaciar(String emailCliente);
}
