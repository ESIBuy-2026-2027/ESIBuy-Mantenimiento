package es.uclm.esibuy.backend.carrito.domain.exception;

/** El producto no existe o no está disponible para comprar (404). */
public class ProductoNoComprableException extends RuntimeException {

    public ProductoNoComprableException(String mensaje) {
        super(mensaje);
    }
}
