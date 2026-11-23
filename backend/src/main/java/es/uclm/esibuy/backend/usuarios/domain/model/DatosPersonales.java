package es.uclm.esibuy.backend.usuarios.domain.model;

import java.util.regex.Pattern;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record DatosPersonales(String nombre, String apellidos, String imagenPerfil) {

    private static final int LONGITUD_MINIMA = 2;
    private static final int LONGITUD_MAXIMA = 30;
    private static final Pattern FORMATO_NOMBRE =
            Pattern.compile("\\p{L}[\\p{L}\\p{M}]*(?:[ '-][\\p{L}\\p{M}]+)*");

    public DatosPersonales {
        nombre = validarNombre(nombre, "El nombre");
        apellidos = validarNombre(apellidos, "Los apellidos");
    }

    private static String validarNombre(String valor, String campo) {
        if (valor == null) {
            throw new UsuarioInvalidoException(campo
                    + " debe tener entre 2 y 30 caracteres y contener solo letras, espacios, guiones o apóstrofos");
        }

        String normalizado = valor.strip();
        if (normalizado.length() < LONGITUD_MINIMA
                || normalizado.length() > LONGITUD_MAXIMA
                || !FORMATO_NOMBRE.matcher(normalizado).matches()) {
            throw new UsuarioInvalidoException(campo
                    + " debe tener entre 2 y 30 caracteres y contener solo letras, espacios, guiones o apóstrofos");
        }
        return normalizado;
    }
}
