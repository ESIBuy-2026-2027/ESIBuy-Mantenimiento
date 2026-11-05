package es.uclm.esibuy.backend.carrito.domain.exception;

/** Una cantidad del carrito que no es un entero mayor que 0 (400). */
public class CantidadInvalidaException extends RuntimeException {

    public CantidadInvalidaException(String mensaje) {
        super(mensaje);
    }
}
