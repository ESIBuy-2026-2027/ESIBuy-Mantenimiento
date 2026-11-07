package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda una lista de deseos en MongoDB (colección listas_deseos, HU14). El índice único por
 * usuario lo crea InicializadorColecciones. El campo usuarioId es el del esquema de la colección: guarda el id del
 * cliente. Solo guarda qué productos y cuándo se añadieron: sus datos se consultan siempre.
 */
@Document(collection = "listas_deseos")
public class ListaDeseosDocument {

    @Id
    private String id;
    private String usuarioId;
    private List<ItemDeseoDocument> items = new ArrayList<>();
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public List<ItemDeseoDocument> getItems() { return items; }
    public void setItems(List<ItemDeseoDocument> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
