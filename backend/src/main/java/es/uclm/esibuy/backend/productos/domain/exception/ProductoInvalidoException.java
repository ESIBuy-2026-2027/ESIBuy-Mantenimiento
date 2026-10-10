package es.uclm.esibuy.backend.productos.domain.exception;

public class ProductoInvalidoException extends RuntimeException {

    public ProductoInvalidoException(String mensaje) {
        super(mensaje);
    }

}
