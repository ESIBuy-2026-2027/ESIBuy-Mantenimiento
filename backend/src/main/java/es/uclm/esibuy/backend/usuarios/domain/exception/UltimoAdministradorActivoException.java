package es.uclm.esibuy.backend.usuarios.domain.exception;

// Se intenta bloquear al último administrador activo (HU23).
public class UltimoAdministradorActivoException extends RuntimeException {

    public UltimoAdministradorActivoException(String message) {
        super(message);
    }
}
