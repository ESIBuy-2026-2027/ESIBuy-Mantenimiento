package es.uclm.esibuy.backend.alertas.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

/**
 * Respuesta de {@code PATCH /api/customer/alerts/{id}/read} (AB#531): un mensaje de éxito y la alerta, ya leída. Los
 * errores siguen el formato común {@code {mensaje, code}} de RespuestaError.
 */
public record AlertaLeidaResponse(String mensaje, AlertaResponse alerta) {

    static final String MARCADA_COMO_LEIDA = "Alerta marcada como leída";

    public static AlertaLeidaResponse de(Alerta alerta) {
        return new AlertaLeidaResponse(MARCADA_COMO_LEIDA, AlertaResponse.de(alerta));
    }
}
