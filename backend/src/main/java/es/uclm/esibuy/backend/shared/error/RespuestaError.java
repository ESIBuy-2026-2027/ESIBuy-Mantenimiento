package es.uclm.esibuy.backend.shared.error;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Cuerpo JSON de las respuestas de error: {@code {"mensaje": "..."}}.
 *
 * <p>El mensaje es siempre genérico y seguro: nunca incluye datos del usuario, trazas ni
 * detalles internos (requisitos de seguridad, «Gestión de errores genéricos»).
 * 
 * <p>{@code code} es un identificador fijo que el frontend usa para elegir su mensaje
 * (p. ej. {@code ACCOUNT_DISABLED}, HU2). Es opcional: si es null no aparece en el JSON.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record RespuestaError(String mensaje, String code) {

    public RespuestaError(String mensaje) { // <- Sobrecarga de constructores para que las llamadas antiguas sigan funcionando, Constructor no canónico
        this(mensaje, null);
    }
}
