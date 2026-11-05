package es.uclm.esibuy.backend.pedidos.domain.exception;

/** El carrito no tiene productos del proveedor del pedido (409 CART_EMPTY). */
public class CarritoSinProductosDelProveedorException extends RuntimeException {

    public CarritoSinProductosDelProveedorException(String mensaje) {
        super(mensaje);
    }
}
