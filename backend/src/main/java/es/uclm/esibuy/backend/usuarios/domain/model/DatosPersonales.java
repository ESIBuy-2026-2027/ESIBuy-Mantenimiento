package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record DatosPersonales(String nombre, String apellidos, String imagenPerfil) {

    private static final int LONGITUD_MAXIMA = 30;

    public DatosPersonales {
        exigirTexto(nombre, "El nombre es obligatorio y tiene un máximo de 30 caracteres");
        exigirTexto(apellidos, "Los apellidos son obligatorios y tienen un máximo de 30 caracteres");
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank() || valor.length() > LONGITUD_MAXIMA) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }
}
