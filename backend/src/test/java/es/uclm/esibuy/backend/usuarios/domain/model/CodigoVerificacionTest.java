package es.uclm.esibuy.backend.usuarios.domain.model;

import es.uclm.esibuy.backend.usuarios.domain.exception.CodigoVerificacionInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Login, paso 2: reglas del código de verificación por email (HU2, T21/T22).
// Válido 10 minutos, de un solo uso y se agota tras 5 intentos fallidos (plan de la T21, D3).
class CodigoVerificacionTest {

    // Hora fija para los tests: el código recibe la hora en vez de consultarla él mismo, como Usuario.
    private static final Instant AHORA = Instant.parse("2026-10-08T10:00:00Z");

    @Test
    @DisplayName("HU2-CP06: un código recién creado se puede usar")
    void codigoNuevoSePuedeUsar() {
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);

        assertTrue(codigo.puedeUsarse(AHORA));
    }

    @Test
    @DisplayName("HU2-CP08: el código caduca a los 10 minutos")
    void caducaALosDiezMinutos() {
        // Frontera: a los 9:59 se puede usar; a los 10:00, no.
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);

        assertTrue(codigo.puedeUsarse(AHORA.plus(Duration.ofMinutes(10)).minusSeconds(1)));
        assertFalse(codigo.puedeUsarse(AHORA.plus(Duration.ofMinutes(10))));
    }

    @Test
    @DisplayName("HU2-CP09: un código ya usado no se puede volver a usar")
    void noSePuedeUsarDosVeces() {
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);

        codigo.marcarUsado(AHORA);

        assertFalse(codigo.puedeUsarse(AHORA));
    }

    @Test
    @DisplayName("HU2-CP09: al marcar el código como usado se guarda la hora de uso")
    void marcarUsadoGuardaLaHoraDeUso() {
        // Trazabilidad (T232): se sabe cuándo se usó el código, no solo que se usó.
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);

        codigo.marcarUsado(AHORA.plusSeconds(30));

        assertEquals(AHORA.plusSeconds(30), codigo.getUsadoEn());
    }

    @Test
    @DisplayName("HU2-CP10: el código se agota tras 5 intentos fallidos")
    void seAgotaTrasCincoIntentosFallidos() {
        // Frontera: con 4 fallos aún se puede usar; con 5, no.
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);
        for (int intento = 0; intento < 4; intento++) {
            codigo.registrarIntentoFallido();
        }
        assertTrue(codigo.puedeUsarse(AHORA));

        codigo.registrarIntentoFallido();

        assertFalse(codigo.puedeUsarse(AHORA));
    }

    @Test
    @DisplayName("HU2-CP09: reconstruir un código guardado conserva su id, su caducidad, su uso y sus intentos")
    void reconstruirConservaElEstado() {
        // T232: lo usa la persistencia al leer un código de MongoDB.
        Instant expiraEn = AHORA.plus(Duration.ofMinutes(10));
        Instant usadoEn = AHORA.plusSeconds(30);

        CodigoVerificacion codigo = CodigoVerificacion.reconstruir("c-1", "u-1", "hash-del-codigo", expiraEn,
                usadoEn, 2);

        assertEquals("c-1", codigo.getId());
        assertEquals("u-1", codigo.getUsuarioId());
        assertEquals("hash-del-codigo", codigo.getCodigoHash());
        assertEquals(expiraEn, codigo.getExpiraEn());
        assertEquals(usadoEn, codigo.getUsadoEn());
        assertEquals(2, codigo.getIntentosFallidos());
        assertFalse(codigo.puedeUsarse(AHORA));
    }

    @Test
    @DisplayName("HU2-CP10: rechaza reconstruir un código con intentos fallidos negativos")
    void rechazaReconstruirConIntentosNegativos() {
        // Un documento corrupto no debe convertirse en un código válido (P6 del plan de la T232).
        Instant expiraEn = AHORA.plus(Duration.ofMinutes(10));

        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> CodigoVerificacion.reconstruir("c-1", "u-1", "hash-del-codigo", expiraEn, null, -1));
    }

    @Test
    @DisplayName("HU2-CP12: rechaza un código sin usuario o sin hash")
    void rechazaCodigoSinUsuarioOSinHash() {
        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> CodigoVerificacion.nuevo(null, "hash-del-codigo", AHORA));
        assertThrows(CodigoVerificacionInvalidoException.class,
                () -> CodigoVerificacion.nuevo("u-1", null, AHORA));
    }
}
