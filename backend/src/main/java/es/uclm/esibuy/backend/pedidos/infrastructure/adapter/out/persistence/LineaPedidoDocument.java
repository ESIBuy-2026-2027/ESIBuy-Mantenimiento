package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.math.BigDecimal;

import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;

/** Una línea del pedido dentro de su documento, con los campos que pide la colección pedidos (lineas). */
public class LineaPedidoDocument {

    private String productoId;
    private String nombreProducto;
    private String vendedorId;
    private int cantidad;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal precioUnitario;
    @Field(targetType = FieldType.DECIMAL128)
    private BigDecimal subtotal;

    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getNombreProducto() { return nombreProducto; }
    public void setNombreProducto(String nombreProducto) { this.nombreProducto = nombreProducto; }
    public String getVendedorId() { return vendedorId; }
    public void setVendedorId(String vendedorId) { this.vendedorId = vendedorId; }
    public int getCantidad() { return cantidad; }
    public void setCantidad(int cantidad) { this.cantidad = cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public void setPrecioUnitario(BigDecimal precioUnitario) { this.precioUnitario = precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }
}
