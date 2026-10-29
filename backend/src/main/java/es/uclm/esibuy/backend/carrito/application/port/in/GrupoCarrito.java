package es.uclm.esibuy.backend.carrito.application.port.in;

import java.math.BigDecimal;
import java.util.List;

/** Los productos de un mismo proveedor dentro del carrito y su total. */
public record GrupoCarrito(String vendedorId, String nombreComercial, BigDecimal total,
        List<LineaCarritoDetallada> lineas) {
}
