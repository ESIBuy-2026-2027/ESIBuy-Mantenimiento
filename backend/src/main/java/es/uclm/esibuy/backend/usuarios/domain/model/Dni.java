package es.uclm.esibuy.backend.usuarios.domain.model;

import java.util.regex.Pattern;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record Dni(String valor) {
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{8}[A-Za-z]$");

    public Dni {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new UsuarioInvalidoException("El DNI no tiene un formato válido");
        }
    }
}
