package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Forma en que se guarda una alerta en MongoDB (colección alertas, AB#529). La validación la crea
 * InicializadorColecciones; el índice por cliente y fecha, que sirve para listar las de un cliente de la más reciente
 * a la más antigua, se declara aquí. Los precios van como Decimal128, como los de productos.
 */
@Document(collection = "alertas")
@CompoundIndex(name = "cliente_fecha", def = "{'clienteId': 1, 'fecha': -1}")
public class AlertaDocument {

    @Id
    private String id;
    private String clienteId;
    private String productoId;
    private String tipo;
    private Instant fecha;
    private boolean leida;
    private String nombreProducto;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal precioAnterior;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal precioNuevo;
    @CreatedDate
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public Instant getFecha() { return fecha; }
    public void setFecha(Instant fecha) { this.fecha = fecha; }
    public boolean isLeida() { return leida; }
    public void setLeida(boolean leida) { this.leida = leida; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public BigDecimal getPrecioAnterior() { return precioAnterior; }
    public void setPrecioAnterior(BigDecimal precioAnterior) { this.precioAnterior = precioAnterior; }
    public BigDecimal getPrecioNuevo() { return precioNuevo; }
    public void setPrecioNuevo(BigDecimal precioNuevo) { this.precioNuevo = precioNuevo; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
