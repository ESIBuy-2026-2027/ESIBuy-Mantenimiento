package es.uclm.esibuy.backend.productos.domain.exception;

/** El producto no existe o no es del vendedor que lo pide: no se distingue para no revelar que existe. */
public class ProductoNoEncontradoException extends RuntimeException {

    public ProductoNoEncontradoException() {
        super("El producto no existe");
    }
}
