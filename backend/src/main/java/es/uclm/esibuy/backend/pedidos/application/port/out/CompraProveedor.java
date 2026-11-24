package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.math.BigDecimal;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Lo que el cliente tiene en el carrito de un proveedor, con los precios que ha calculado el servidor. */
public record CompraProveedor(String vendedorId, String nombreComercial, List<LineaPedido> lineas) {

    // Lo que costará el pedido: la suma de las líneas, cada una con el descuento de su producto ya aplicado
    public BigDecimal total() {
        return Pedido.totalDe(lineas);
    }
}
