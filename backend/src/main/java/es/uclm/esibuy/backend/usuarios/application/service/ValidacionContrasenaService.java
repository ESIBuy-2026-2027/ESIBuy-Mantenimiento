
package es.uclm.esibuy.backend.usuarios.application.service;

import java.util.logging.Logger;

import es.uclm.esibuy.backend.usuarios.application.port.out.ComprobadorContrasenaComprometidaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.exception.ComprobacionContrasenaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PoliticaContrasenas;

/**
 * Servicio encargado de centralizar la validación de contraseñas.
 *
 * Primero aplica la política local y después consulta si la contraseña
 * aparece en bases de datos de contraseñas comprometidas.
 *
 * Si el servicio externo no está disponible, se continúa con las
 * comprobaciones locales (fail-open).
 */
public class ValidacionContrasenaService {

    private static final Logger LOGGER = Logger.getLogger(ValidacionContrasenaService.class.getName());

    private final ComprobadorContrasenaComprometidaPort comprobadorContrasena;

    public ValidacionContrasenaService(
            ComprobadorContrasenaComprometidaPort comprobadorContrasena) {
        this.comprobadorContrasena = comprobadorContrasena;
    }

    public void validar(String contrasena, DatosPersonales datosPersonales, Email email) {
        PoliticaContrasenas.validar(contrasena, datosPersonales, email);
        comprobarContrasenaComprometida(contrasena);
    }

    /**
     * Valida la contraseña y comprueba que coincide con su repetición
     * antes de consultar el servicio externo.
     */
    public void validar(String contrasena, String repeticion,
            DatosPersonales datosPersonales, Email email) {
        PoliticaContrasenas.validar(contrasena, datosPersonales, email);

        if (!contrasena.equals(repeticion)) {
            throw new ContrasenaInvalidaException(
                    MotivoRechazo.CONTRASENAS_DISTINTAS,
                    "Las contraseñas no coinciden");
        }

        comprobarContrasenaComprometida(contrasena);
    }

    private void comprobarContrasenaComprometida(String contrasena) {
        try {
            if (comprobadorContrasena.estaComprometida(contrasena)) {
                throw new ContrasenaInvalidaException(
                        MotivoRechazo.CONTRASENA_COMPROMETIDA,
                        "La contraseña aparece en bases de datos de contraseñas comprometidas");
            }
        } catch (ComprobacionContrasenaException e) {
            LOGGER.warning(
                    "No se pudo consultar el servicio de contraseñas comprometidas. "
                            + "Se continúa con la validación local.");
        }
    }

}
