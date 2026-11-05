package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;

/** Lo que el cliente tiene en el carrito de un proveedor, con los precios que ha calculado el servidor. */
public record CompraProveedor(String vendedorId, String nombreComercial, List<LineaPedido> lineas) {
}
