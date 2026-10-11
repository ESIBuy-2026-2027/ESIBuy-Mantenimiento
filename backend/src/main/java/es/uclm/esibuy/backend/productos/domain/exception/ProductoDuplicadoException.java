package es.uclm.esibuy.backend.productos.domain.exception;

/** El vendedor ya tiene un producto con ese nombre (sin distinguir mayúsculas). */
public class ProductoDuplicadoException extends RuntimeException {

    private static final String MENSAJE = "El vendedor ya tiene un producto con ese nombre";

    public ProductoDuplicadoException() {
        super(MENSAJE);
    }

    public ProductoDuplicadoException(Throwable causa) {
        super(MENSAJE, causa);
    }
}
