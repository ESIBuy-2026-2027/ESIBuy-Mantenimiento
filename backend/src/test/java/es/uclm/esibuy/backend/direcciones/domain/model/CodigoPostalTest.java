package es.uclm.esibuy.backend.direcciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CodigoPostalTest {

    @Test
    @DisplayName("HU5-CP01: acepta un código postal de cinco dígitos")
    void creaCodigoPostalDeCincoDigitos() {
        CodigoPostal codigoPostal = new CodigoPostal("13071");

        assertEquals("13071", codigoPostal.valor());
    }

    @Test
    @DisplayName("HU5-CP01: rechaza un código postal de cuatro o de seis dígitos")
    void rechazaCodigoPostalDeCuatroOSeisDigitos() {
        // Fronteras: 4 y 6 dígitos no valen; 5 sí
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal("1307"));
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal("130710"));
    }

    @Test
    @DisplayName("HU5-CP01: rechaza un código postal con letras o espacios")
    void rechazaCodigoPostalConLetrasOEspacios() {
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal("1307A"));
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal("13 071"));
    }

    @Test
    @DisplayName("HU5-CP01: rechaza un código postal nulo o vacío")
    void rechazaCodigoPostalNuloOVacio() {
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal(null));
        assertThrows(DireccionInvalidaException.class, () -> new CodigoPostal(""));
    }
}
