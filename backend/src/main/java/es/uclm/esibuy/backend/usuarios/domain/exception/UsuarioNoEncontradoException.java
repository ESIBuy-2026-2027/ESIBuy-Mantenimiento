package es.uclm.esibuy.backend.usuarios.domain.exception;

// El usuario pedido no existe (p. ej. al activar una cuenta, HU24). La API lo devuelve como 404.
public class UsuarioNoEncontradoException extends RuntimeException {

    public UsuarioNoEncontradoException(String message) {
        super(message);
    }
}
