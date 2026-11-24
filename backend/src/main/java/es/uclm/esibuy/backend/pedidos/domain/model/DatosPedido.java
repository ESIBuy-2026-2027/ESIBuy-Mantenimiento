package es.uclm.esibuy.backend.pedidos.domain.model;

import java.time.Instant;
import java.util.List;

/**
 * Datos necesarios para construir un pedido. El histórico y las fechas de entrega solo los trae un pedido ya
 * guardado; para uno nuevo sirve el constructor sin ellos.
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
        Instant createdAt,
        List<CambioEstado> historial,
        Instant fechaEstimadaEntrega,
        Instant fechaEntrega) {

    public DatosPedido(
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
        this(id, numeroPedido, clienteId, vendedorId, nombreComercial, estado, lineas, direccionEnvio,
                paymentMethodId, createdAt, List.of(), null, null);
    }
}
