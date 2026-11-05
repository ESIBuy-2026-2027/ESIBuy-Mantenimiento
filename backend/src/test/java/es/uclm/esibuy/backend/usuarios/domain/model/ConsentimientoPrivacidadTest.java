package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ConsentimientoPrivacidadTest {

    private static final Instant AHORA = Instant.parse("2026-10-09T10:00:00Z");

    @Test
    @DisplayName("HU1-CP06: crea un consentimiento con la fecha de aceptación y la versión de la política")
    void creaConsentimientoConVersion() {
        ConsentimientoPrivacidad consentimiento = new ConsentimientoPrivacidad(AHORA, "1.0");

        assertEquals(AHORA, consentimiento.aceptadaEn());
        assertEquals("1.0", consentimiento.version());
    }

    @Test
    @DisplayName("HU1-CP06: rechaza un consentimiento sin fecha o sin versión")
    void rechazaConsentimientoIncompleto() {
        assertThrows(UsuarioInvalidoException.class, () -> new ConsentimientoPrivacidad(null, "1.0"));
        assertThrows(UsuarioInvalidoException.class, () -> new ConsentimientoPrivacidad(AHORA, null));
        assertThrows(UsuarioInvalidoException.class, () -> new ConsentimientoPrivacidad(AHORA, "   "));
    }
}
