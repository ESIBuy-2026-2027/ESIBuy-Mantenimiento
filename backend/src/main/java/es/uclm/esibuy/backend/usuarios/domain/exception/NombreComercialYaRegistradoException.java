package es.uclm.esibuy.backend.usuarios.domain.exception;

public class NombreComercialYaRegistradoException extends RuntimeException {

    public NombreComercialYaRegistradoException(String mensaje) {
        super(mensaje);
    }
}
