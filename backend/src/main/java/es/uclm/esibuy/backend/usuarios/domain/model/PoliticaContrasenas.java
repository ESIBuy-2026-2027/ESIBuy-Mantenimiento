package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

public final class PoliticaContrasenas {

    private static final int LONGITUD_MINIMA = 8;
    // BCrypt solo usa los primeros 72 bytes de la contraseña: con 64 caracteres nunca se trunca.
    private static final int LONGITUD_MAXIMA = 64;
    private static final Set<String> LISTA_NEGRA = Set.of(
            "12345678", "password", "password1", "qwertyui", "contrasena", "11111111", "abcdefgh");
    private static final Pattern MAYUSCULA = Pattern.compile("\\p{Lu}");

    private PoliticaContrasenas() {
    }

    public static void validar(String contrasena, DatosPersonales datosPersonales, Email email) {
        exigirLongitud(contrasena);
        exigirComposicion(contrasena);
        String normalizada = contrasena.toLowerCase(Locale.ROOT);
        exigirNoComun(normalizada);
        exigirSinDatosPersonales(normalizada, datosPersonales, email);
    }

    private static void exigirComposicion(String contrasena) {
        if (!MAYUSCULA.matcher(contrasena).find()) {
            throw new ContrasenaInvalidaException("La contraseña debe tener al menos una mayúscula");
        }
    }

    private static void exigirLongitud(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA) {
            throw new ContrasenaInvalidaException(
                    "La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres");
        }
        if (contrasena.length() > LONGITUD_MAXIMA) {
            throw new ContrasenaInvalidaException(
                    "La contraseña no puede tener más de " + LONGITUD_MAXIMA + " caracteres");
        }
    }

    private static void exigirNoComun(String normalizada) {
        if (LISTA_NEGRA.contains(normalizada)) {
            throw new ContrasenaInvalidaException("La contraseña es demasiado común");
        }
    }

    private static void exigirSinDatosPersonales(String normalizada, DatosPersonales datosPersonales, Email email) {
        String parteLocalEmail = email.valor().substring(0, email.valor().indexOf('@'));
        for (String dato : new String[] {datosPersonales.nombre(), datosPersonales.apellidos(), parteLocalEmail}) {
            if (normalizada.contains(dato.toLowerCase(Locale.ROOT))) {
                throw new ContrasenaInvalidaException("La contraseña no puede contener datos personales");
            }
        }
    }
}
