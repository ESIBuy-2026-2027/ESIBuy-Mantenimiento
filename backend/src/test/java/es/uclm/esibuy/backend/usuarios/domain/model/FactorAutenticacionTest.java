package es.uclm.esibuy.backend.usuarios.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Reglas del factor de Microsoft Authenticator de un usuario (HU2, T20), en el dominio: JUnit puro,
// sin Spring. El factor nace sin confirmar al empezar el alta; el primer código correcto lo confirma.
// Para no aceptar dos veces el mismo código se guarda el periodo de 30 s del último aceptado (D5).
class FactorAutenticacionTest {

    private static final Instant AHORA = Instant.parse("2026-10-11T10:00:00Z");

    @Test
    @DisplayName("HU2-CP23: un factor recién creado no está confirmado")
    void unFactorNuevoNoEstaConfirmado() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");

        assertFalse(factor.estaConfirmado());
        assertNull(factor.getVerificadoEn());
    }

    @Test
    @DisplayName("HU2-CP23: el primer código correcto confirma el alta y guarda cuándo")
    void elPrimerUsoConfirmaElAlta() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");

        factor.registrarUso(5, AHORA);

        assertTrue(factor.estaConfirmado());
        assertEquals(AHORA, factor.getVerificadoEn());
    }

    @Test
    @DisplayName("HU2-CP23: los usos siguientes no cambian la fecha del alta")
    void losUsosSiguientesNoCambianLaFechaDelAlta() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");
        factor.registrarUso(5, AHORA);

        factor.registrarUso(7, AHORA.plus(Duration.ofMinutes(1)));

        assertEquals(AHORA, factor.getVerificadoEn());
    }

    @Test
    @DisplayName("HU2-CP20: un código de un periodo igual o anterior al último usado no vale; uno posterior sí")
    void noSePuedeReutilizarUnPeriodo() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");
        factor.registrarUso(5, AHORA);

        assertFalse(factor.puedeUsarPeriodo(5), "El mismo código (mismo periodo) no se puede reutilizar");
        assertFalse(factor.puedeUsarPeriodo(4), "Un código anterior al último usado tampoco");
        assertTrue(factor.puedeUsarPeriodo(6));
    }

    @Test
    @DisplayName("HU2-CP20: un factor que aún no se ha usado acepta cualquier periodo")
    void unFactorSinUsosAceptaCualquierPeriodo() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");

        assertTrue(factor.puedeUsarPeriodo(1));
    }
}
