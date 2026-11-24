package es.uclm.esibuy.backend.pedidos.domain.model;

import java.time.Instant;

/** Un paso del histórico de un pedido: el estado al que pasó y cuándo. */
public record CambioEstado(EstadoPedido estado, Instant fecha) {
}
