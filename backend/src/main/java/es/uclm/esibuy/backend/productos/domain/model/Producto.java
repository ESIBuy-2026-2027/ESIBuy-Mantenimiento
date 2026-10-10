package es.uclm.esibuy.backend.productos.domain.model;

import java.math.BigDecimal;

public class Producto {

    public Producto(String id, String vendedorId, String nombre, String descripcion, BigDecimal precio,
            int stock, String categoriaId, String imagen, boolean disponible, Descuento descuento,
            Descuento descuentoPremium) {
        // esqueleto: las validaciones y la asignación de campos las harás en verde
    }

    public String getId() { return null; }
    public String getVendedorId() { return null; }
    public String getNombre() { return null; }
    public String getDescripcion() { return null; }
    public BigDecimal getPrecio() { return null; }
    public int getStock() { return 0; }
    public String getCategoriaId() { return null; }
    public String getImagen() { return null; }
    public boolean isDisponible() { return false; }
    public Descuento getDescuento() { return null; }
    public Descuento getDescuentoPremium() { return null; }
}