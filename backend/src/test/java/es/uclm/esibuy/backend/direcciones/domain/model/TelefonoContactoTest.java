package es.uclm.esibuy.backend.direcciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelefonoContactoTest {

    @Test
    @DisplayName("HU5-CP02: acepta un teléfono de nueve dígitos")
    void creaTelefonoDeNueveDigitos() {
        TelefonoContacto telefono = new TelefonoContacto("612345678");

        assertEquals("612345678", telefono.valor());
    }

    @Test
    @DisplayName("HU5-CP02: rechaza un teléfono de ocho o de diez dígitos")
    void rechazaTelefonoDeOchoODiezDigitos() {
        // Fronteras: 8 y 10 dígitos no valen; 9 sí. Sin prefijo de país (solo España)
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto("61234567"));
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto("6123456789"));
    }

    @Test
    @DisplayName("HU5-CP02: rechaza un teléfono con letras o con prefijo")
    void rechazaTelefonoConLetrasOPrefijo() {
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto("61234567A"));
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto("+34612345678"));
    }

    @Test
    @DisplayName("HU5-CP02: rechaza un teléfono nulo o vacío")
    void rechazaTelefonoNuloOVacio() {
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto(null));
        assertThrows(DireccionInvalidaException.class, () -> new TelefonoContacto(""));
    }
}
