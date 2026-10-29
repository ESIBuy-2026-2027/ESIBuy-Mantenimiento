package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/**
 * Forma en que se guarda un pedido en MongoDB (colección pedidos, HU10). Los importes van como Decimal128: el
 * validador de la colección exige un número, y así no hay redondeos. Los índices, también el único de
 * numeroPedido, los crea InicializadorColecciones. createdAt lo pone el dominio: es la fecha del pedido.
 */
@Document(collection = "pedidos")
public class PedidoDocument {

    @Id
    private String id;
    private String numeroPedido;
    private String clienteId;
    private String vendedorId;
    private String nombreComercial;
    private String estado;
    private List<LineaPedidoDocument> lineas = new ArrayList<>();
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal total;
    private DireccionEnvioDocument direccionEnvio;
    private String paymentMethodId;
    private Instant createdAt;
    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNumeroPedido() { return numeroPedido; }
    public void setNumeroPedido(String numeroPedido) { this.numeroPedido = numeroPedido; }
    public String getClienteId() { return clienteId; }
    public void setClienteId(String clienteId) { this.clienteId = clienteId; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public String getNombreComercial() { return nombreComercial; }
    public void setNombreComercial(String nombreComercial) { this.nombreComercial = nombreComercial; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public List<LineaPedidoDocument> getLineas() { return lineas; }
    public void setLineas(List<LineaPedidoDocument> lineas) { this.lineas = lineas; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public DireccionEnvioDocument getDireccionEnvio() { return direccionEnvio; }
    public void setDireccionEnvio(DireccionEnvioDocument direccionEnvio) { this.direccionEnvio = direccionEnvio; }
    public String getPaymentMethodId() { return paymentMethodId; }
    public void setPaymentMethodId(String paymentMethodId) { this.paymentMethodId = paymentMethodId; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Instant updatedAt) { this.updatedAt = updatedAt; }
}
