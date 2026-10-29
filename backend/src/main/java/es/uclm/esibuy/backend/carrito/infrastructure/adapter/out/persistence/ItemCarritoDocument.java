package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import java.time.Instant;

/** Una línea del carrito dentro de su documento (campo items de la colección carritos). */
public class ItemCarritoDocument {

    private String productoId;
    private int cantidad;
    private Instant addedAt;
    private Instant updatedAt;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public Instant getAddedAt() { return addedAt; }
    public void setAddedAt(Instant addedAt) { this.addedAt = addedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
