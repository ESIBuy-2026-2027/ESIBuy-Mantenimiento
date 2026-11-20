package es.uclm.esibuy.backend.pedidos.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Datos necesarios para construir un pedido.
 */
public record DatosPedido(
        String id,
        NumeroPedido numeroPedido,
        String clienteId,
        String vendedorId,
        String nombreComercial,
        EstadoPedido estado,
        List<LineaPedido> lineas,
        DireccionEnvioPedido direccionEnvio,
        String paymentMethodId,
        Instant createdAt) {
}