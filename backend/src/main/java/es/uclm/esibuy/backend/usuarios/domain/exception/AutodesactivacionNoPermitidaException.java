package es.uclm.esibuy.backend.usuarios.domain.exception;

// Un administrador intenta desactivar su propia cuenta (HU23).
public class AutodesactivacionNoPermitidaException extends RuntimeException {

    public AutodesactivacionNoPermitidaException(String message) {
        super(message);
    }
}
