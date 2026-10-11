package es.uclm.esibuy.backend.productos.domain.exception;

public class ProductoDuplicadoException extends RuntimeException {

    public ProductoDuplicadoException(Throwable cause) {
        super("El vendedor ya tiene un producto con ese nombre", cause);
    }
}