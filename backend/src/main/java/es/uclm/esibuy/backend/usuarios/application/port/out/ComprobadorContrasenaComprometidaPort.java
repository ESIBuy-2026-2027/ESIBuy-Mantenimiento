
package es.uclm.esibuy.backend.usuarios.application.port.out;

/**
 * Puerto para comprobar si una contraseña aparece en bases de datos
 * de contraseñas comprometidas.
 *
 * La implementación consulta un servicio externo, como Have I Been Pwned.
 */
public interface ComprobadorContrasenaComprometidaPort {

    /**
     * Comprueba si la contraseña aparece en el conjunto de contraseñas
     * comprometidas.
     *
     * @param contrasena contraseña en texto plano que se comprobará
     * @return true si está comprometida; false si no aparece
     * @throws ComprobacionContrasenaException si no se puede completar
     *                                         la consulta externa
     */
    boolean estaComprometida(String contrasena);
}
