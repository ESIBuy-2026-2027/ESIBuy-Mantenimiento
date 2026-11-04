package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.time.Instant;

/** Un producto de la lista dentro de su documento (campo items de la colección listas_deseos). */
public class ItemDeseoDocument {

    private String productoId;
    private Instant addedAt;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
}
