package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

/**
 * Forma en que se guarda el factor de Microsoft Authenticator de un usuario en MongoDB
 * (colección factores_autenticacion, HU2, T20). El secreto TOTP va cifrado (totpSecretEncrypted).
 * lastUsedStep es el periodo de 30 s del último código aceptado, para no aceptarlo dos veces (D5).
 */
@Document(collection = "factores_autenticacion")
public class FactorAutenticacionDocument {

    @Id
    private String id;
    private final String usuarioId;
    private final String totpSecretEncrypted;
    private boolean totpEnabled;
    private Instant verifiedAt;
    private Long lastUsedStep;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public FactorAutenticacionDocument(String usuarioId, String totpSecretEncrypted) {
        this.usuarioId = usuarioId;
        this.totpSecretEncrypted = totpSecretEncrypted;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public String getTotpSecretEncrypted() { return totpSecretEncrypted; }
    public boolean isTotpEnabled() { return totpEnabled; }
    public void setTotpEnabled(boolean totpEnabled) { this.totpEnabled = totpEnabled; }
    public Instant getVerifiedAt() { return verifiedAt; }
    public void setVerifiedAt(Instant verifiedAt) { this.verifiedAt = verifiedAt; }
    public Long getLastUsedStep() { return lastUsedStep; }
    public void setLastUsedStep(Long lastUsedStep) { this.lastUsedStep = lastUsedStep; }
    public Instant getCreatedAt() { return createdAt; }
    // Al actualizar se conserva la fecha de creación del documento guardado (como en la T232)
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}