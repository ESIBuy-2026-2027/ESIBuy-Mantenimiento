package es.uclm.esibuy.backend.usuarios.domain.exception;

// Un administrador intenta cambiar el rol de su propia cuenta (HU22, T43).
public class CambioDePropioRolException extends RuntimeException {

    public CambioDePropioRolException(String message) {
        super(message);
    }
}
