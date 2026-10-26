package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TelefonoBizumTest {

    @Test
    @DisplayName("HU6-CP05: acepta un teléfono de nueve dígitos")
    void creaTelefonoDeNueveDigitos() {
        TelefonoBizum telefono = new TelefonoBizum("612345678");

        assertEquals("612345678", telefono.valor());
    }

    @Test
    @DisplayName("HU6-CP05: rechaza un teléfono con letras")
    void rechazaTelefonoConLetras() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new TelefonoBizum("61234567A"));
    }

    @Test
    @DisplayName("HU6-CP05: rechaza un teléfono de ocho dígitos")
    void rechazaTelefonoDeOchoDigitos() {
        // Frontera: 8 dígitos no valen; 9 sí. Sin prefijo de país (solo España), como el teléfono del usuario
        assertThrows(MetodoPagoInvalidoException.class, () -> new TelefonoBizum("61234567"));
    }

    @Test
    @DisplayName("HU6-CP05: rechaza un teléfono nulo o vacío")
    void rechazaTelefonoNuloOVacio() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new TelefonoBizum(null));
        assertThrows(MetodoPagoInvalidoException.class, () -> new TelefonoBizum(""));
    }

    @Test
    @DisplayName("HU6-CP06: la máscara muestra solo los últimos tres dígitos")
    void enmascaraTodoMenosLosUltimosTresDigitos() {
        TelefonoBizum telefono = new TelefonoBizum("612345678");

        assertEquals("•••• 678", telefono.mascara());
    }
}
