package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

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
        // TODO: new Dni("12345678") lanza UsuarioInvalidoException.
        fail("Funcionalidad no implementada");
    }

    @Test
    @DisplayName("HU1-CP15: rechaza un DNI de siete números")
    void rechazaDniConSieteNumeros() {
        // TODO: frontera: new Dni("1234567Z") lanza UsuarioInvalidoException.
        fail("Funcionalidad no implementada");
    }
}
