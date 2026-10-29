package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Lo que la API devuelve de un pedido, con el contrato que ya usa el frontend (frontend/src/api/orders.js) más el
 * número de pedido, la dirección de envío y la fecha. No lleva el cliente: es el de la sesión.
 */
public record PedidoResponse(String id, String numeroPedido, Vendedor vendedor, BigDecimal total, String estado,
        String paymentMethodId, DireccionEnvioPedido direccionEnvio, List<Linea> lineas, Instant createdAt) {

    public record Vendedor(String id, String nombreComercial) {
    }

    public record Linea(String productoId, String nombre, int cantidad, BigDecimal precioUnitario,
            BigDecimal subtotal) {
    }

    public static PedidoResponse desde(Pedido pedido) {
        return new PedidoResponse(pedido.getId(), pedido.getNumeroPedido().valor(),
                new Vendedor(pedido.getVendedorId(), pedido.getNombreComercial()), pedido.getTotal(),
                pedido.getEstado().name(), pedido.getPaymentMethodId(), pedido.getDireccionEnvio(),
                pedido.getLineas().stream().map(PedidoResponse::aLinea).toList(), pedido.getCreatedAt());
    }

    private static Linea aLinea(LineaPedido linea) {
        return new Linea(linea.productoId(), linea.nombreProducto(), linea.cantidad(), linea.precioUnitario(),
                linea.subtotal());
    }
}
