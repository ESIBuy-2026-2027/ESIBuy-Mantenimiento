package es.uclm.esibuy.backend.metodospago.domain.model;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.util.regex.Pattern;

public record NumeroTarjeta(String valor) {

    // Entre 13 y 19 dígitos: el rango de longitudes de las tarjetas según ISO/IEC 7812
    private static final Pattern FORMATO = Pattern.compile("^[0-9]{13,19}$");

    public NumeroTarjeta {
        if (valor == null || !FORMATO.matcher(valor).matches() || !cumpleLuhn(valor)) {
            throw new MetodoPagoInvalidoException("El número de tarjeta no es válido");
        }
    }

    // Lo único del número que puede mostrarse o guardarse en claro
    public String mascara() {
        return "•••• " + valor.substring(valor.length() - 4);
    }

    // Algoritmo de Luhn: desde la derecha se duplica uno de cada dos dígitos (restando 9 si pasa de 9)
    // y la suma total tiene que ser múltiplo de 10.
    private static boolean cumpleLuhn(String digitos) {
        int suma = 0;
        boolean duplicar = false;
        for (int i = digitos.length() - 1; i >= 0; i--) {
            int digito = digitos.charAt(i) - '0';
            if (duplicar) {
                digito *= 2;
                if (digito > 9) {
                    digito -= 9;
                }
            }
            suma += digito;
            duplicar = !duplicar;
        }
        return suma % 10 == 0;
    }
}
