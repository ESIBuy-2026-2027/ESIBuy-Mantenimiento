package es.uclm.esibuy.backend.pedidos.domain.exception;

/** El método de pago no es del cliente en sesión (400). */
public class MetodoPagoNoValidoException extends RuntimeException {

    public MetodoPagoNoValidoException(String mensaje) {
        super(mensaje);
    }
}
