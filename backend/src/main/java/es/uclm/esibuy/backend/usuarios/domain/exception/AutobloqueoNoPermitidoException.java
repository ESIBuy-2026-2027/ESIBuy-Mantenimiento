package es.uclm.esibuy.backend.usuarios.domain.exception;

// Un administrador intenta bloquear su propia cuenta (HU23).
public class AutobloqueoNoPermitidoException extends RuntimeException {

    public AutobloqueoNoPermitidoException(String message) {
        super(message);
    }
}
