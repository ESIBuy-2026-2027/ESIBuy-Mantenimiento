package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.CambioEstado;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Lo que la API devuelve del seguimiento del envío de un pedido: su estado, las fechas de entrega (null si no
 * están disponibles) y el histórico de estados, del primero al último.
 */
public record SeguimientoEnvioResponse(String pedidoId, String estado, Instant fechaEstimadaEntrega,
        Instant fechaEntrega, List<Paso> historial) {

    public record Paso(String estado, Instant fecha) {
    }

    public static SeguimientoEnvioResponse desde(Pedido pedido) {
        return new SeguimientoEnvioResponse(pedido.getId(), pedido.getEstado().name(),
                pedido.getFechaEstimadaEntrega(), pedido.getFechaEntrega(),
                pedido.getHistorial().stream().map(SeguimientoEnvioResponse::aPaso).toList());
    }

    private static Paso aPaso(CambioEstado cambio) {
        return new Paso(cambio.estado().name(), cambio.fecha());
    }
}
