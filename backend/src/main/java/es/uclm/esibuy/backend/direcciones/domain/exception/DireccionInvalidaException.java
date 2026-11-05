package es.uclm.esibuy.backend.direcciones.domain.exception;

public class DireccionInvalidaException extends RuntimeException {

    public DireccionInvalidaException(String mensaje) {
        super(mensaje);
    }
}
