package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.seguridad;

import es.uclm.esibuy.backend.usuarios.application.port.out.TotpPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.OptionalLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test del adaptador real con la librería java-totp (T20, D2), sin Spring y sin mocks.
// Usa los vectores de test oficiales del RFC 6238 (apéndice B): un secreto y unas horas fijas con el
// código exacto que debe salir. Si la librería los reproduce, calcula el TOTP igual que Microsoft
// Authenticator. El RFC da códigos de 8 dígitos; Authenticator usa 6, que son los 6 últimos.
class JavaTotpAdapterTest {

    // Secreto del RFC 6238 para SHA-1 ("12345678901234567890") codificado en Base32, que es como lo
    // guardan las apps de autenticación y como lo espera la librería
    private static final String SECRETO_RFC = "GEZDGNBVGY3TQOJQGEZDGNBVGY3TQOJQ";

    private final TotpPort totp = new JavaTotpAdapter();

    @Test
    @DisplayName("HU2-CP18: acepta el código del RFC 6238 y dice en qué periodo de 30 segundos es válido")
    void aceptaElCodigoDelRfcEnSuPeriodo() {
        // A los 59 s desde 1970 el código es 94287082 (8 dígitos) → 287082; periodo 59 / 30 = 1
        assertEquals(OptionalLong.of(1), totp.periodoDelCodigo(SECRETO_RFC, "287082", Instant.ofEpochSecond(59)));
        // Otro vector del RFC, con un cero a la izquierda: 07081804 → 081804; periodo 1111111109 / 30
        assertEquals(OptionalLong.of(37_037_036),
                totp.periodoDelCodigo(SECRETO_RFC, "081804", Instant.ofEpochSecond(1_111_111_109L)));
    }

    @Test
    @DisplayName("HU2-CP19: rechaza un código de Authenticator incorrecto")
    void rechazaUnCodigoIncorrecto() {
        assertTrue(totp.periodoDelCodigo(SECRETO_RFC, "123456", Instant.ofEpochSecond(59)).isEmpty());
    }

    @Test
    @DisplayName("HU2-CP21: acepta el código del periodo anterior o siguiente (±30 s) pero no uno más lejano")
    void aceptaUnPeriodoDeDesfaseYNoMas() {
        // El código 287082 es el del periodo 1 (de 30 s a 59 s)
        // 30 s después (periodo 2): el reloj del móvil va retrasado → se acepta, y sigue siendo el periodo 1
        assertEquals(OptionalLong.of(1), totp.periodoDelCodigo(SECRETO_RFC, "287082", Instant.ofEpochSecond(89)));
        // 30 s antes (periodo 0): el reloj del móvil va adelantado → se acepta
        assertEquals(OptionalLong.of(1), totp.periodoDelCodigo(SECRETO_RFC, "287082", Instant.ofEpochSecond(29)));
        // 60 s después (periodo 3): demasiado lejos → se rechaza
        assertTrue(totp.periodoDelCodigo(SECRETO_RFC, "287082", Instant.ofEpochSecond(119)).isEmpty());
    }
}
