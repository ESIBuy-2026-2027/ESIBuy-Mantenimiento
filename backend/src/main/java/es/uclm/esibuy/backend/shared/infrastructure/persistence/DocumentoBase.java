package es.uclm.esibuy.backend.shared.infrastructure.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;

/**
 * Campos comunes a los documentos de todas las colecciones («Planteamiento inicial BBDD»,
 * «Convenciones comunes»): {@code _id}, {@code createdAt} y {@code updatedAt}.
 *
 * <p>Los documentos de MongoDB heredan de esta clase para no repetirlos. Las fechas las rellena
 * Spring Data con la auditoría activada en {@code MongoConfig}.
 */
public abstract class DocumentoBase {

    @Id
    private String id;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() {
        return id;
    }

    // Con el id, guardar un documento ya existente lo actualiza en vez de insertar otro.
    public void setId(String id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    // Al actualizar hay que conservar la fecha de creación: si el documento se rehace desde el dominio,
    // que no la conoce, Spring solo rellena createdAt en los documentos nuevos.
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
