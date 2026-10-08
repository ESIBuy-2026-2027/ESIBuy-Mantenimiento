package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import java.util.Locale;
import java.util.Set;

public final class PoliticaContrasenas {

    private static final int LONGITUD_MINIMA = 8;
    private static final Set<String> LISTA_NEGRA = Set.of(
            "12345678", "password", "password1", "qwertyui", "contrasena", "11111111", "abcdefgh");

    private PoliticaContrasenas() {
    }

    public static void validar(String contrasena, String nombre, String apellidos, Email email) {
        exigirLongitudMinima(contrasena);
        String normalizada = contrasena.toLowerCase(Locale.ROOT);
        exigirNoComun(normalizada);
        exigirSinDatosPersonales(normalizada, nombre, apellidos, email);
    }

    private static void exigirLongitudMinima(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA) {
            throw new ContrasenaInvalidaException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
    }

    private static void exigirNoComun(String normalizada) {
        if (LISTA_NEGRA.contains(normalizada)) {
            throw new ContrasenaInvalidaException("La contraseña es demasiado común");
        }
    }

    private static void exigirSinDatosPersonales(String normalizada, String nombre, String apellidos, Email email) {
        String parteLocalEmail = email.valor().substring(0, email.valor().indexOf('@'));
        for (String dato : new String[] {nombre, apellidos, parteLocalEmail}) {
            if (normalizada.contains(dato.toLowerCase(Locale.ROOT))) {
                throw new ContrasenaInvalidaException("La contraseña no puede contener datos personales");
            }
        }
    }
}
