package es.uclm.esibuy.backend.usuarios.dominio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    void creaEmailConFormatoValido() {
        Email email = new Email("ana@esibuy.es");

        assertEquals("ana@esibuy.es", email.valor());
    }

    @Test
    void dosEmailsConElMismoValorSonIguales() {
        Email primero = new Email("ana@esibuy.es");
        Email segundo = new Email("ana@esibuy.es");

        assertEquals(primero, segundo);
    }

    @Test
    void rechazaEmailNulo() {
        assertThrows(EmailInvalidoException.class, () -> new Email(null));
    }

    @Test
    void rechazaEmailEnBlanco() {
        assertThrows(EmailInvalidoException.class, () -> new Email("   "));
    }

    @Test
    void rechazaEmailSinArroba() {
        assertThrows(EmailInvalidoException.class, () -> new Email("ana.esibuy.es"));
    }

    @Test
    void rechazaEmailSinDominio() {
        assertThrows(EmailInvalidoException.class, () -> new Email("ana@"));
    }
}
