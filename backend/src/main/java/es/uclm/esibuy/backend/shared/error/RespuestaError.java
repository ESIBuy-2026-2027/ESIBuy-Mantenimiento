package es.uclm.esibuy.backend.shared.error;

/**
 * Cuerpo JSON de las respuestas de error: {@code {"mensaje": "..."}}.
 *
 * <p>El mensaje es siempre genérico y seguro: nunca incluye datos del usuario, trazas ni
 * detalles internos (requisitos de seguridad, «Gestión de errores genéricos»).
 */
public record RespuestaError(String mensaje) {
}
