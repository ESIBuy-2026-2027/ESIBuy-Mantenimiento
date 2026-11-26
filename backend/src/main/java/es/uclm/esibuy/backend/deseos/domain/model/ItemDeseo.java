package es.uclm.esibuy.backend.deseos.domain.model;

import java.time.Instant;

/**
 * Un producto de la lista de deseos, cuándo se añadió y la cantidad deseada (por defecto 1). Solo guarda el
 * identificador del producto: el nombre y el precio se consultan siempre al módulo de productos.
 */
public record ItemDeseo(String productoId, Instant addedAt, int cantidad) {

    public ItemDeseo {
        if (cantidad < 1) {
            cantidad = 1;
        }
    }

    public ItemDeseo(String productoId, Instant addedAt) {
        this(productoId, addedAt, 1);
    }
}
