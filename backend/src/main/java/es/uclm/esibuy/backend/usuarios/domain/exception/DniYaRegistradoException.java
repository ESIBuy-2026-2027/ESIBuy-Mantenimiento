package es.uclm.esibuy.backend.usuarios.domain.exception;

public class DniYaRegistradoException extends RuntimeException {

    public DniYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
