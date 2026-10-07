package es.uclm.esibuy.backend.usuarios.dominio;

public class UsuarioInvalidoException extends RuntimeException {

    public UsuarioInvalidoException(String mensaje) {
        super(mensaje);
    }
}
