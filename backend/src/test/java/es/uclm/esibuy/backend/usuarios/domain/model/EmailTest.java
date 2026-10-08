package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.EmailInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EmailTest {

    @Test
    @DisplayName("HU1-CP07: acepta un email con formato válido")
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
    @DisplayName("HU1-CP07: rechaza un email nulo")
    void rechazaEmailNulo() {
        assertThrows(EmailInvalidoException.class, () -> new Email(null));
    }

    @Test
    @DisplayName("HU1-CP07: rechaza un email en blanco")
    void rechazaEmailEnBlanco() {
        assertThrows(EmailInvalidoException.class, () -> new Email("   "));
    }

    @Test
    @DisplayName("HU1-CP07: rechaza un email sin arroba")
    void rechazaEmailSinArroba() {
        assertThrows(EmailInvalidoException.class, () -> new Email("ana.esibuy.es"));
    }

    @Test
    @DisplayName("HU1-CP07: rechaza un email sin dominio")
    void rechazaEmailSinDominio() {
        assertThrows(EmailInvalidoException.class, () -> new Email("ana@"));
    }

    @Test
    @DisplayName("HU1-CP07: rechaza un email sin punto en el dominio")
    void rechazaEmailSinPuntoEnElDominio() {
        // Frontera en la que discrepan la validación web (@Email acepta "ana@localhost") y el dominio
        assertThrows(EmailInvalidoException.class, () -> new Email("ana@localhost"));
    }
}
