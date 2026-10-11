package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Forma en que se guarda un producto en MongoDB (colección productos, HU31, T202). Los importes
 * van como Decimal128: el validador de la colección exige un número, y así no hay redondeos.
 */
@Document(collection = "productos")
public class ProductoDocument {

    @Id
    private String id;
    private String vendedorId;
    private String nombre;
    private String descripcion;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal precio;
    private int stock;
    private String categoriaId;
    private String imagen;
    private boolean disponible;
    private DescuentoDocument descuento;
    private DescuentoDocument descuentoPremium;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public void setPrecio(BigDecimal precio) { this.precio = precio; }
    public int getStock() { return stock; }
    public void setStock(int stock) { this.stock = stock; }
    public String getCategoriaId() { return categoriaId; }
    public void setCategoriaId(String categoriaId) { this.categoriaId = categoriaId; }
    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
    public DescuentoDocument getDescuento() { return descuento; }
    public void setDescuento(DescuentoDocument descuento) { this.descuento = descuento; }
    public DescuentoDocument getDescuentoPremium() { return descuentoPremium; }
    public void setDescuentoPremium(DescuentoDocument descuentoPremium) { this.descuentoPremium = descuentoPremium; }
    public Instant getCreatedAt() { return createdAt; }
    // Al actualizar se conserva la fecha de creación del documento guardado (como en FactorAutenticacion)
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /** Descuento guardado como subdocumento {activo, porcentaje}; sin descuento, el porcentaje es null. */
    public static class DescuentoDocument {

        private boolean activo;
        @Field(targetType = FieldType.DECIMAL128)
        private BigDecimal porcentaje;

        public boolean isActivo() { return activo; }
        public void setActivo(boolean activo) { this.activo = activo; }
        public BigDecimal getPorcentaje() { return porcentaje; }
        public void setPorcentaje(BigDecimal porcentaje) { this.porcentaje = porcentaje; }
    }
}
