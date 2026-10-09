package es.uclm.esibuy.backend.usuarios.application.port.out.exception;

/**
 * No se ha podido enviar un correo (T231, D8 y D10). Es un fallo técnico del exterior (SMTP, red,
 * proveedor), no una regla de negocio: por eso vive junto a los puertos de salida y no en
 * {@code domain.exception}. La lanza el adaptador en lugar de la excepción de Spring, para que la
 * capa de aplicación no dependa de Spring.
 */
public class EnvioEmailException extends RuntimeException {

    public EnvioEmailException(String message, Throwable cause) {
        super(message, cause);
    }
}