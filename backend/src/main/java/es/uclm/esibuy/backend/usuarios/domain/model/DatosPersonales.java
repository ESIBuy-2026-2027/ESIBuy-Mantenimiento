
package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record DatosPersonales(String nombre, String apellidos, String imagenPerfil) {

    private static final int LONGITUD_MINIMA = 2;
    private static final int LONGITUD_MAXIMA = 30;

    private static final String MENSAJE_ERROR = " debe tener entre 2 y 30 caracteres y contener solo letras, espacios, guiones o apóstrofos";

    public DatosPersonales {
        nombre = validarNombre(nombre, "El nombre");
        apellidos = validarNombre(apellidos, "Los apellidos");
    }

    private static String validarNombre(String valor, String campo) {
        if (valor == null) {
            throw new UsuarioInvalidoException(campo + MENSAJE_ERROR);
        }

        String normalizado = valor.strip();

        if (normalizado.length() < LONGITUD_MINIMA
                || normalizado.length() > LONGITUD_MAXIMA
                || !formatoValido(normalizado)) {
            throw new UsuarioInvalidoException(campo + MENSAJE_ERROR);
        }

        return normalizado;
    }

    private static boolean formatoValido(String valor) {
        int[] caracteres = valor.codePoints().toArray();

        if (caracteres.length == 0 || !Character.isLetter(caracteres[0])) {
            return false;
        }

        boolean separadorAnterior = false;

        for (int i = 1; i < caracteres.length; i++) {
            int caracter = caracteres[i];

            if (esLetraOMarca(caracter)) {
                separadorAnterior = false;
            } else if (esSeparador(caracter)) {
                if (separadorAnterior || i == caracteres.length - 1) {
                    return false;
                }

                separadorAnterior = true;
            } else {
                return false;
            }
        }

        return true;
    }

    private static boolean esLetraOMarca(int caracter) {
        int tipo = Character.getType(caracter);

        return Character.isLetter(caracter)
                || tipo == Character.NON_SPACING_MARK
                || tipo == Character.COMBINING_SPACING_MARK
                || tipo == Character.ENCLOSING_MARK;
    }

    private static boolean esSeparador(int caracter) {
        return caracter == ' '
                || caracter == '-'
                || caracter == '\'';
    }
}
