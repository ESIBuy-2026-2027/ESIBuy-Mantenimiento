package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Conversión entre el factor de Authenticator del dominio y su documento de MongoDB
// (factores_autenticacion, HU2, T20). Sin Spring ni base de datos.
class FactorAutenticacionDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-10-11T10:00:00Z");

    private final FactorAutenticacionDocumentMapper mapper = new FactorAutenticacionDocumentMapper();

    @Test
    @DisplayName("HU2-CP24: el documento guarda el secreto cifrado, el alta y el último periodo usado")
    void convierteFactorEnDocumento() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");
        factor.registrarUso(5, AHORA);

        FactorAutenticacionDocument documento = mapper.aDocumento(factor);

        assertEquals("u-1", documento.getUsuarioId());
        assertEquals("secreto-cifrado", documento.getTotpSecretEncrypted());
        assertTrue(documento.isTotpEnabled());
        assertEquals(AHORA, documento.getVerifiedAt());
        assertEquals(5L, documento.getLastUsedStep());
    }

    @Test
    @DisplayName("HU2-CP23: un documento leído recupera el factor con su alta y su último periodo")
    void convierteDocumentoEnFactor() {
        FactorAutenticacionDocument documento = new FactorAutenticacionDocument("u-1", "secreto-cifrado");
        documento.setId("f-1");
        documento.setTotpEnabled(true);
        documento.setVerifiedAt(AHORA);
        documento.setLastUsedStep(5L);

        FactorAutenticacion factor = mapper.aDominio(documento);

        assertEquals("f-1", factor.getId());
        assertEquals("u-1", factor.getUsuarioId());
        assertEquals("secreto-cifrado", factor.getSecretoCifrado());
        assertTrue(factor.estaConfirmado());
        assertEquals(AHORA, factor.getVerificadoEn());
        assertFalse(factor.puedeUsarPeriodo(5), "Conserva el último periodo usado");
        assertTrue(factor.puedeUsarPeriodo(6));
    }
}
