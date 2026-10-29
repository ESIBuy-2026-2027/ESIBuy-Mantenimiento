package es.uclm.esibuy.backend.carrito.application.port.in;

import java.math.BigDecimal;
import java.util.List;

/** El carrito tal como lo ve el cliente: agrupado por proveedor y con los totales ya calculados. */
public record CarritoDetallado(List<GrupoCarrito> grupos, int totalProductos, BigDecimal total) {
}
