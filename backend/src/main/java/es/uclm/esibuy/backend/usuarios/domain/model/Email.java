package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import java.util.Locale;
import java.util.regex.Pattern;

public record Email(String valor) {

    private static final Pattern FORMATO = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");

    public Email {
        if (valor == null || valor.isBlank()) {
            throw new EmailInvalidoException("El email es obligatorio");
        }
        // Normalizado (T240): sin espacios en los extremos y en minúsculas, para que el mismo buzón
        // escrito de otra forma no sea otra cuenta. Locale.ROOT evita depender del idioma del sistema.
        valor = valor.strip().toLowerCase(Locale.ROOT);
        if (!FORMATO.matcher(valor).matches()) {
            throw new EmailInvalidoException("El email no tiene un formato válido: " + valor);
        }
    }
}
