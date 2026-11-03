package es.uclm.esibuy.backend.usuarios.domain.exception;

// La contraseña actual no coincide al confirmar una operación sobre la propia cuenta (p. ej. eliminarla, HU25).
// La API lo devuelve como 400 con code WRONG_PASSWORD: la sesión sigue abierta.
public class ContrasenaActualIncorrectaException extends RuntimeException {

    public ContrasenaActualIncorrectaException(String message) {
        super(message);
    }
}
