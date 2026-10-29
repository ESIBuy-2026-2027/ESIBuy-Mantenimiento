package es.uclm.esibuy.backend.direcciones.domain.exception;

public class DireccionNoEncontradaException extends RuntimeException {

    public DireccionNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
