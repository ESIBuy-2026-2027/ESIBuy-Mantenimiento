package es.uclm.esibuy.backend.deseos.application.port.in;

import java.util.List;

/**
 * Quién tiene un producto en sus favoritos, para avisarle de sus cambios (alertas, AB#529). No es un caso de uso del
 * cliente sobre su lista: lo usa el sistema cuando un vendedor cambia un producto o un cliente cancela un pedido.
 */
public interface FavoritosProductoUseCase {

    /** Ids de los clientes que tienen el producto en sus favoritos (no en sus listas de deseos), sin repetir. */
    List<String> clientesConFavorito(String productoId);
}
