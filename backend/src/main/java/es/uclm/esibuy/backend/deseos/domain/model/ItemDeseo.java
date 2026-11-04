package es.uclm.esibuy.backend.deseos.domain.model;

import java.time.Instant;

/**
 * Un producto de la lista de deseos y cuándo se añadió. Solo guarda el identificador del producto: el nombre y el
 * precio se consultan siempre al módulo de productos, para no enseñar datos viejos.
 */
public record ItemDeseo(String productoId, Instant addedAt) {
}
