package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/**
 * Una alerta en la API (AB#529): su tipo ({@code BAJADA_PRECIO}, {@code DISPONIBILIDAD} o {@code PRODUCTO_NUEVO}),
 * fecha, si está leída y el producto como era en ese momento. Los precios solo los lleva una bajada de precio.
 */
public record AlertaResponse(String id, String tipo, Instant fecha, boolean leida, Producto producto) {

    public record Producto(String id, String nombre, BigDecimal precioAnterior, BigDecimal precioNuevo) {
    }

    static AlertaResponse de(Alerta alerta) {
        return new AlertaResponse(alerta.getId(), alerta.getTipo().name(), alerta.getFecha(), alerta.isLeida(),
                new Producto(alerta.getProductoId(), alerta.getDetalle().nombreProducto(),
                        alerta.getDetalle().precioAnterior(), alerta.getDetalle().precioNuevo()));
    }
}
