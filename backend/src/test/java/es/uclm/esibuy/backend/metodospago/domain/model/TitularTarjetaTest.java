package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TitularTarjetaTest {

    @Test
    @DisplayName("HU6-CP03: acepta un titular con nombre y apellidos")
    void creaTitularValido() {
        // Acentos, guion y apóstrofo son válidos en un nombre
        TitularTarjeta titular = new TitularTarjeta("María O'Neill-López");

        assertEquals("María O'Neill-López", titular.valor());
    }

    @Test
    @DisplayName("HU6-CP03: rechaza un titular vacío o nulo")
    void rechazaTitularVacioONulo() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta(null));
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta(""));
        // Solo espacios cuenta como vacío
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta("   "));
    }

    @Test
    @DisplayName("HU6-CP03: rechaza un titular que supera la longitud máxima")
    void rechazaTitularDemasiadoLargo() {
        // Frontera: 26 caracteres valen (límite del nombre grabado en la tarjeta); 27 no
        assertDoesNotThrow(() -> new TitularTarjeta("A".repeat(26)));
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta("A".repeat(27)));
    }

    @Test
    @DisplayName("HU6-CP03: rechaza un titular con dígitos o símbolos")
    void rechazaTitularConDigitosOSimbolos() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta("Juan Pérez 2"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta("Juan@Pérez"));
        assertThrows(MetodoPagoInvalidoException.class, () -> new TitularTarjeta("<script>"));
    }
}
