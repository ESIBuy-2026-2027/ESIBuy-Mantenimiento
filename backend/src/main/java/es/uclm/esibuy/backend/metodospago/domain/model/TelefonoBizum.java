package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.util.regex.Pattern;

public record TelefonoBizum(String valor) {

    // Nueve dígitos sin prefijo de país. Duplica la regla del Telefono de usuarios a propósito: un módulo
    // no puede importar el dominio de otro.
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{9}$");

    public TelefonoBizum {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new MetodoPagoInvalidoException("El teléfono de Bizum no es válido");
        }
    }

    // Lo único del teléfono que puede mostrarse o guardarse en claro
    public String mascara() {
        return "•••• " + valor.substring(valor.length() - 3);
    }
}
