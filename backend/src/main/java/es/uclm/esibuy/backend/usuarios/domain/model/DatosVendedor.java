package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

// Perfil del vendedor (T241): una sola categoría principal (decisión del equipo) y el teléfono opcional.
public record DatosVendedor(String nombreComercial, String categoriaPrincipalId, Dni dni, Telefono telefono)
        implements PerfilUsuario {

    private static final int LONGITUD_MAXIMA = 30;

    public DatosVendedor {
        exigirTexto(nombreComercial, "El nombre comercial es obligatorio y no puede superar los 30 caracteres");
        exigirTexto(categoriaPrincipalId, "La categoría principal es obligatoria");
        exigirNoNulo(dni, "El DNI es obligatorio");
    }

    private static void exigirTexto(String valor, String mensaje) {
        if (valor == null || valor.isBlank() || valor.length() > LONGITUD_MAXIMA) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }

    private static void exigirNoNulo(Object valor, String mensaje) {
        if (valor == null) {
            throw new UsuarioInvalidoException(mensaje);
        }
    }
}
