package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelefonoTest {

    @Test
    @DisplayName("HU1-CP15: acepta un teléfono de nueve dígitos")
    void creaTelefonoDeNueveDigitos() {
        Telefono telefono = new Telefono("612345678");

        assertEquals("612345678", telefono.valor());
    }

    @Test
    @DisplayName("HU1-CP15: rechaza un teléfono con letras")
    void rechazaTelefonoConLetras() {
        assertThrows(UsuarioInvalidoException.class, () -> new Telefono("61234567A"));
    }

    @Test
    @DisplayName("HU1-CP15: rechaza un teléfono de ocho dígitos")
    void rechazaTelefonoDeOchoDigitos() {
        // Frontera: 8 dígitos no valen; 9 sí. Sin prefijo de país (solo España)
        assertThrows(UsuarioInvalidoException.class, () -> new Telefono("61234567"));
    }
}
