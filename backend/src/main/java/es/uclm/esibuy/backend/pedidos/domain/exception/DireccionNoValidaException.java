package es.uclm.esibuy.backend.pedidos.domain.exception;

/** La dirección de envío no es del cliente en sesión (400). */
public class DireccionNoValidaException extends RuntimeException {

    public DireccionNoValidaException(String mensaje) {
        super(mensaje);
    }
}
