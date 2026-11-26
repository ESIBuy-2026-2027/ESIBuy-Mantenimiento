package es.uclm.esibuy.backend.alertas.domain.model;

/**
 * Tipos de alerta del cliente: bajada de precio y disponibilidad de un producto favorito (AB#529) y producto nuevo de
 * una categoría favorita (AB#530).
 */
public enum TipoAlerta {
    BAJADA_PRECIO,
    DISPONIBILIDAD,
    PRODUCTO_NUEVO
}
