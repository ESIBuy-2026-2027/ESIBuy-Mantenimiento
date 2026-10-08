package es.uclm.esibuy.backend.usuarios.domain.exception;

public class CuentaDesactivadaException extends RuntimeException {

    public CuentaDesactivadaException(String message) {
        super(message);
    }
}
