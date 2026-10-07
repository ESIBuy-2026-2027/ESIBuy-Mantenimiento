package es.uclm.esibuy.backend.usuarios.dominio;

public class ContrasenaInvalidaException extends RuntimeException {

    public ContrasenaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
