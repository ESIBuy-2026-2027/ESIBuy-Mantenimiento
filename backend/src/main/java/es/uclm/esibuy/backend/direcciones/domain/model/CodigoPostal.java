package es.uclm.esibuy.backend.direcciones.domain.model;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import java.util.regex.Pattern;

public record CodigoPostal(String valor) {

    // Cinco dígitos: solo se envía a España
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{5}$");

    public CodigoPostal {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new DireccionInvalidaException("El código postal no es válido");
        }
    }
}
