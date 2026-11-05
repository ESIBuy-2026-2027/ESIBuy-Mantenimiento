package es.uclm.esibuy.backend.direcciones.domain.model;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import java.util.regex.Pattern;

public record TelefonoContacto(String valor) {

    // Nueve dígitos sin prefijo de país. Duplica la regla del Telefono de usuarios a propósito: un módulo
    // no puede importar el dominio de otro.
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{9}$");

    public TelefonoContacto {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new DireccionInvalidaException("El teléfono de contacto no es válido");
        }
    }
}
