package es.uclm.esibuy.backend.metodospago.domain.exception;

public class MetodoPagoInvalidoException extends RuntimeException {

    public MetodoPagoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
