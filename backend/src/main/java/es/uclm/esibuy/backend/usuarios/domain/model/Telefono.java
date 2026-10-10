package es.uclm.esibuy.backend.usuarios.domain.model;

import java.util.regex.Pattern;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;

public record Telefono(String valor) {

    private static final Pattern PATRON = Pattern.compile("^[0-9]{9}$");

    public Telefono {
        if (valor == null || !PATRON.matcher(valor).matches()) {
            throw new UsuarioInvalidoException("El teléfono no tiene un formato válido");
        }
    }
}
