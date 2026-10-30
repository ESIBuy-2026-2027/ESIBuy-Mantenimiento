package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

public record DetalleProductoCatalogo(
    String id,
    String nombre,
    String descripcion,
    String imagen,
    String categoriaId,
    String vendedorId,
    String nombreComercial,
    BigDecimal precio,
    BigDecimal porcentajeDescuento,
    BigDecimal precioFinal,
    int stock,
    boolean disponible
) {

}
