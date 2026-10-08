package es.uclm.esibuy.backend.usuarios.domain.exception;

public class UsuarioInvalidoException extends RuntimeException {

    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
