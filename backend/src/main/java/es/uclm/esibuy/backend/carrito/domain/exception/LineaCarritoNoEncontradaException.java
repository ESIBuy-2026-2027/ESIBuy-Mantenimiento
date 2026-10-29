package es.uclm.esibuy.backend.carrito.domain.exception;

/** El producto no está en el carrito del cliente (404). */
public class LineaCarritoNoEncontradaException extends RuntimeException {

    public LineaCarritoNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
