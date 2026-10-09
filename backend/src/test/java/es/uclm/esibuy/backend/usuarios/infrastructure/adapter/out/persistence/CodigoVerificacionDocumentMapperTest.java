package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

// Test unitario del mapper dominio <-> documento de MongoDB de los códigos de verificación (HU2, T232).
// El documento sigue el modelo de tokens_seguridad: purpose LOGIN_2FA, tokenHash, expiresAt, attempts y usedAt.
class CodigoVerificacionDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-09T10:00:00Z");

    private final CodigoVerificacionDocumentMapper mapper = new CodigoVerificacionDocumentMapper();

    @Test
    @DisplayName("HU2-CP12: el documento guarda el hash del código asociado al usuario, para el login")
    void convierteCodigoEnDocumento() {
        CodigoVerificacion codigo = CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA);

        CodigoVerificacionDocument documento = mapper.aDocumento(codigo);

        assertNull(documento.getId());
        assertEquals("u-1", documento.getUsuarioId());
        assertEquals("hash-del-codigo", documento.getTokenHash());
        assertEquals("LOGIN_2FA", documento.getPurpose());
        assertEquals(AHORA.plus(Duration.ofMinutes(10)), documento.getExpiresAt());
        assertEquals(0, documento.getAttempts());
        assertNull(documento.getUsedAt());
    }

    @Test
    @DisplayName("HU2-CP09: el código leído conserva su id, su caducidad, su uso y sus intentos fallidos")
    void convierteDocumentoEnCodigo() {
        Instant expiraEn = AHORA.plus(Duration.ofMinutes(10));
        CodigoVerificacionDocument documento =
                new CodigoVerificacionDocument("u-1", "hash-del-codigo", "LOGIN_2FA", expiraEn);
        documento.setId("c-1");
        documento.setAttempts(2);
        documento.setUsedAt(AHORA.plusSeconds(30));

        CodigoVerificacion codigo = mapper.aDominio(documento);

        assertEquals("c-1", codigo.getId());
        assertEquals("u-1", codigo.getUsuarioId());
        assertEquals("hash-del-codigo", codigo.getCodigoHash());
        assertEquals(expiraEn, codigo.getExpiraEn());
        assertEquals(AHORA.plusSeconds(30), codigo.getUsadoEn());
        assertEquals(2, codigo.getIntentosFallidos());
        assertFalse(codigo.puedeUsarse(AHORA));
    }
}
