
package es.uclm.esibuy.backend.usuarios.application.port.out.exception;

/**
 * Error técnico al consultar un servicio externo de comprobación
 * de contraseñas comprometidas, como Have I Been Pwned.
 *
 * No representa un rechazo de la contraseña, sino un fallo de
 * comunicación o de respuesta del servicio externo.
 */
public class ComprobacionContrasenaException extends RuntimeException {

    public ComprobacionContrasenaException(String message, Throwable cause) {
        super(message, cause);
    }

    public ComprobacionContrasenaException(String message) {
        super(message);
    }
}
