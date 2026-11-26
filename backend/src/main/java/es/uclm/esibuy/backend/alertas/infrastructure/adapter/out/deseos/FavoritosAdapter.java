package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.deseos;

import java.util.List;

import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.deseos.application.port.in.FavoritosProductoUseCase;

/** Adaptador de salida que pregunta al módulo de deseos por su puerto de entrada, sin usar su dominio. */
public class FavoritosAdapter implements FavoritosPort {

    private final FavoritosProductoUseCase favoritosProducto;

    public FavoritosAdapter(FavoritosProductoUseCase favoritosProducto) {
        this.favoritosProducto = favoritosProducto;
    }

    @Override
    public List<String> clientesConFavorito(String productoId) {
        return favoritosProducto.clientesConFavorito(productoId);
    }
}
