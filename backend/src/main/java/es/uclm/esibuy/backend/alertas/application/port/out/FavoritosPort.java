package es.uclm.esibuy.backend.alertas.application.port.out;

import java.util.List;

/** Lo que las alertas necesitan de los favoritos de los clientes (módulo de deseos). */
public interface FavoritosPort {

    /** Ids de los clientes que tienen el producto en sus favoritos (no en sus listas de deseos), sin repetir. */
    List<String> clientesConFavorito(String productoId);
}
