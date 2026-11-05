package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un código de verificación del login en MongoDB (colección
 * {@code tokens_seguridad}, HU2, T232).
 *
 * <p>Es distinta de {@code CodigoVerificacion} del dominio, que no conoce MongoDB. Los nombres de los
 * campos siguen el «Planteamiento inicial BBDD». La colección la comparten los códigos del login y los
 * tokens de verificación de email y de recuperación de contraseña, que se distinguen por {@code purpose}.
 * Solo se guarda el hash del código, nunca el código en claro. {@code expiresAt} tiene un índice TTL:
 * MongoDB borra el documento cuando caduca.
 */
@Document(collection = "tokens_seguridad")
public class CodigoVerificacionDocument {

    @Id
    private String id;
    private final String usuarioId;
    private final String tokenHash;
    private final String purpose;
    private final Instant expiresAt;
    private int attempts;
    private Instant usedAt;
    @CreatedDate // «Convenciones comunes» del documento de BBDD; lo rellena Spring (MongoConfig)
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public CodigoVerificacionDocument(String usuarioId, String tokenHash, String purpose, Instant expiresAt) {
        this.usuarioId = usuarioId;
        this.tokenHash = tokenHash;
        this.purpose = purpose;
        this.expiresAt = expiresAt;
    }

    public String getId() {
        return id;
    }

    // Con el id, guardar un código ya existente lo actualiza en vez de insertar otro.
    public void setId(String id) {
        this.id = id;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public String getTokenHash() {
        return tokenHash;
    }

    public String getPurpose() {
        return purpose;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public int getAttempts() {
        return attempts;
    }

    public void setAttempts(int attempts) {
        this.attempts = attempts;
    }

    public Instant getUsedAt() {
        return usedAt;
    }

    public void setUsedAt(Instant usedAt) {
        this.usedAt = usedAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Al actualizar un código hay que conservar su fecha de creación: el documento se rehace desde
    // el dominio, que no la conoce, y Spring solo rellena createdAt en los documentos nuevos.
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
