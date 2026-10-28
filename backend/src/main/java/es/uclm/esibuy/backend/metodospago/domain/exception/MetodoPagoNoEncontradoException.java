package es.uclm.esibuy.backend.metodospago.domain.exception;

public class MetodoPagoNoEncontradoException extends RuntimeException {

    public MetodoPagoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
