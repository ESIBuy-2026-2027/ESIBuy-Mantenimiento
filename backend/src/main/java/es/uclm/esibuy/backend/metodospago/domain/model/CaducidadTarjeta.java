package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.time.YearMonth;
import java.util.regex.Pattern;

// Solo valida el formato. La vigencia depende del mes actual y se comprueba con vigenteEn(): así una
// tarjeta guardada que ya ha caducado puede seguir cargándose desde la base de datos.
public record CaducidadTarjeta(String valor) {

    // MM/AA con el mes entre 01 y 12
    private static final Pattern FORMATO = Pattern.compile("^(0[1-9]|1[0-2])/[0-9]{2}$");

    public CaducidadTarjeta {
        if (valor == null || !FORMATO.matcher(valor).matches()) {
            throw new MetodoPagoInvalidoException("La caducidad de la tarjeta no es válida");
        }
    }

    // La tarjeta es válida durante todo su mes de caducidad; deja de serlo el mes siguiente
    public boolean vigenteEn(YearMonth mesActual) {
        return !mesDeCaducidad().isBefore(mesActual);
    }

    // "MM/AA": el año se interpreta en el siglo XXI (27 -> 2027)
    private YearMonth mesDeCaducidad() {
        int mes = Integer.parseInt(valor.substring(0, 2));
        int anio = 2000 + Integer.parseInt(valor.substring(3, 5));
        return YearMonth.of(anio, mes);
    }
}
