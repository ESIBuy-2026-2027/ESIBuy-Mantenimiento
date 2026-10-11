package es.uclm.esibuy.backend.usuarios.domain.exception;

// Registro del vendedor (T241): el correo y el nombre comercial ya existen a la vez, y se comunican juntos.
public class CorreoYNombreComercialYaRegistradosException extends RuntimeException {

    public CorreoYNombreComercialYaRegistradosException(String mensaje) {
        super(mensaje);
    }
}
