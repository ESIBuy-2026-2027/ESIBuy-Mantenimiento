package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DniTest {

    @Test
    @DisplayName("HU1-CP15: acepta un DNI de 8 números y una letra")
    void creaDniConFormatoValido() {
        Dni dni = new Dni("12345678Z");

        assertEquals("12345678Z", dni.valor());
    }

    @Test
    @DisplayName("HU1-CP15: rechaza un DNI sin letra")
    void rechazaDniSinLetra() {
        assertThrows(UsuarioInvalidoException.class, () -> new Dni("12345678"));
    }

    @Test
    @DisplayName("HU1-CP15: rechaza un DNI de siete números")
    void rechazaDniConSieteNumeros() {
        // Frontera: 7 números no valen; 8 sí (ciclo 1)
        assertThrows(UsuarioInvalidoException.class, () -> new Dni("1234567Z"));
    }
}
