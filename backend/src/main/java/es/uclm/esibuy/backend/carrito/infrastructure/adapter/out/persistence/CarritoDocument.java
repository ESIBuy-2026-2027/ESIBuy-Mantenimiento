package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda un carrito en MongoDB (colección carritos, HU9). El índice único por cliente lo crea
 * InicializadorColecciones. Solo guarda qué productos y cuántas unidades: el precio se consulta siempre.
 */
@Document(collection = "carritos")
public class CarritoDocument {

    @Id
    private String id;
    private String clienteId;
    private List<ItemCarritoDocument> items = new ArrayList<>();
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public List<ItemCarritoDocument> getItems() { return items; }
    public void setItems(List<ItemCarritoDocument> items) { this.items = items; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
