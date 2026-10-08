package es.uclm.esibuy.backend.usuarios.domain.exception;

public class EmailInvalidoException extends RuntimeException {

    public EmailInvalidoException(String mensaje) {
        super(mensaje);
    }
}
