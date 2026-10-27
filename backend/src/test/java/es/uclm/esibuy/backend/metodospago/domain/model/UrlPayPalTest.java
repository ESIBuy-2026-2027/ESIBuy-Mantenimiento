package es.uclm.esibuy.backend.metodospago.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.metodospago.domain.exception.MetodoPagoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class UrlPayPalTest {

    @Test
    @DisplayName("HU6-CP04: acepta una URL HTTPS válida")
    void creaUrlValida() {
        UrlPayPal url = new UrlPayPal("https://paypal.me/juanperez");

        assertEquals("https://paypal.me/juanperez", url.valor());
    }

    @Test
    @DisplayName("HU6-CP04: rechaza un texto que no es una URL")
    void rechazaTextoQueNoEsUnaUrl() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new UrlPayPal("esto no es una url"));
        // Esquema sin servidor
        assertThrows(MetodoPagoInvalidoException.class, () -> new UrlPayPal("https://"));
    }

    @Test
    @DisplayName("HU6-CP04: rechaza una URL nula o vacía")
    void rechazaUrlNulaOVacia() {
        assertThrows(MetodoPagoInvalidoException.class, () -> new UrlPayPal(null));
        assertThrows(MetodoPagoInvalidoException.class, () -> new UrlPayPal(""));
    }

    @Test
    @DisplayName("HU6-CP06: la máscara muestra solo el dominio de la URL")
    void enmascaraMostrandoSoloElDominio() {
        // La ruta identifica la cuenta del cliente: no se muestra
        UrlPayPal url = new UrlPayPal("https://paypal.me/juanperez");

        assertEquals("paypal.me", url.mascara());
    }
}
