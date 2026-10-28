package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.util.regex.Pattern;

public record TitularTarjeta(String valor) {

    // Límite del nombre grabado en la tarjeta (ISO/IEC 7813)
    private static final int LONGITUD_MAXIMA = 26;
    // Letras de cualquier alfabeto (acentos y ñ incluidos), espacios, guion y apóstrofo
    private static final Pattern CARACTERES = Pattern.compile("^[\\p{L} '-]+$");

    public TitularTarjeta {
        if (valor == null || valor.isBlank()) {
            throw new MetodoPagoInvalidoException("El titular de la tarjeta es obligatorio");
        }
        if (valor.length() > LONGITUD_MAXIMA) {
            throw new MetodoPagoInvalidoException(
                    "El titular de la tarjeta no puede superar " + LONGITUD_MAXIMA + " caracteres");
        }
        if (!CARACTERES.matcher(valor).matches()) {
            throw new MetodoPagoInvalidoException("El titular de la tarjeta contiene caracteres no válidos");
        }
    }
}
