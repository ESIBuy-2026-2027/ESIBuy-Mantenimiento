package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/**
 * Respuesta de {@code GET /api/customer/alerts} (AB#529): un mensaje de éxito y las alertas del cliente, de la más
 * reciente a la más antigua. Los errores siguen el formato común {@code {mensaje, code}} de RespuestaError.
 */
public record AlertasResponse(String mensaje, List<AlertaResponse> alertas) {

    static final String CON_ALERTAS = "Alertas consultadas correctamente";
    static final String SIN_ALERTAS = "No tienes alertas";

    public static AlertasResponse de(List<Alerta> alertas) {
        return new AlertasResponse(alertas.isEmpty() ? SIN_ALERTAS : CON_ALERTAS,
                alertas.stream().map(AlertaResponse::de).toList());
    }
}
