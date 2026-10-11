package es.uclm.esibuy.backend.productos.domain.exception;

public class ProductoDuplicadoException extends RuntimeException {

    public ProductoDuplicadoException() {
        super("El vendedor ya tiene un producto con ese nombre");
    }
}