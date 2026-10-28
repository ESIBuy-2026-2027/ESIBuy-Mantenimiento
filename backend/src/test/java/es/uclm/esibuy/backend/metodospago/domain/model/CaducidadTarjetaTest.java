package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import java.time.YearMonth;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CaducidadTarjetaTest {

    // Mes de referencia fijo para que los tests no dependan de la fecha en que se ejecutan
    private static final YearMonth OCTUBRE_2026 = YearMonth.of(2026, 10);

    @Test
    @DisplayName("HU6-CP02: una caducidad posterior al mes actual está vigente")
    void caducidadFuturaEstaVigente() {
        CaducidadTarjeta caducidad = new CaducidadTarjeta("12/27");

        assertTrue(caducidad.vigenteEn(OCTUBRE_2026));
    }

    @Test
    @DisplayName("HU6-CP02: una caducidad del mes actual está vigente")
    void caducidadDelMesActualEstaVigente() {
        // Frontera: la tarjeta sigue siendo válida durante todo su mes de caducidad
        CaducidadTarjeta caducidad = new CaducidadTarjeta("10/26");

        assertTrue(caducidad.vigenteEn(OCTUBRE_2026));
    }

    @Test
    @DisplayName("HU6-CP02: una caducidad del mes anterior no está vigente")
    void caducidadDelMesAnteriorNoEstaVigente() {
        CaducidadTarjeta caducidad = new CaducidadTarjeta("09/26");

        assertFalse(caducidad.vigenteEn(OCTUBRE_2026));
    }

    @Test
    @DisplayName("HU6-CP02: rechaza un mes fuera del rango 01-12")
    void rechazaMesFueraDeRango() {
        // Fronteras: 00 y 13 no son meses
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("00/27"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("13/27"));
    }

    @Test
    @DisplayName("HU6-CP02: rechaza una caducidad que no tiene formato MM/AA")
    void rechazaFormatoIncorrecto() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("1/27"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("01/2027"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("0127"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta("01-27"));
    }

    @Test
    @DisplayName("HU6-CP02: rechaza una caducidad nula o vacía")
    void rechazaCaducidadNulaOVacia() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta(null));
        assertThrows(MetodoPagoInvalidoException.class, () -> new CaducidadTarjeta(""));
    }
}
