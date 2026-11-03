package es.uclm.esibuy.backend.deseos.domain.exception;

/** El producto no existe o no está disponible, así que no se puede añadir a la lista de deseos. */
public class ProductoNoDisponibleException extends RuntimeException {

    public ProductoNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
