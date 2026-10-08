package es.uclm.esibuy.backend.usuarios.domain.exception;

public class CuentaBloqueadaException extends RuntimeException {

    public CuentaBloqueadaException(String message) {
        super(message);
    }
 
}
