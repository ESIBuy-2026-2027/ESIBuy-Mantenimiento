package es.uclm.esibuy.backend.usuarios.domain.exception;

public class CorreoYaRegistradoException extends RuntimeException {

    public CorreoYaRegistradoException(String message) {
        super(message);
    }
    
}
