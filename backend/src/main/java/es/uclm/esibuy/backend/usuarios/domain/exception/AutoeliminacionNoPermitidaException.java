package es.uclm.esibuy.backend.usuarios.domain.exception;

public class AutoeliminacionNoPermitidaException extends RuntimeException {

    public AutoeliminacionNoPermitidaException(String mensaje) {
        super(mensaje);
    }
}