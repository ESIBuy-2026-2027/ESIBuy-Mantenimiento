package es.uclm.esibuy.backend.usuarios.domain.exception;

public class ContrasenaInvalidaException extends RuntimeException {
    public ContrasenaInvalidaException(String mensaje) {
        super(mensaje);
    }

}
