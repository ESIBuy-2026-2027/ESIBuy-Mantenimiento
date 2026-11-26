package es.uclm.esibuy.backend.deseos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.deseos.application.port.in.FavoritosProductoUseCase;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Clientes que tienen un producto en sus favoritos, para las alertas (AB#529). */
public class FavoritosProductoService implements FavoritosProductoUseCase {

    private final ListaDeseosRepositoryPort listaDeseosRepository;

    public FavoritosProductoService(ListaDeseosRepositoryPort listaDeseosRepository) {
        this.listaDeseosRepository = listaDeseosRepository;
    }

    // Lo consulta el vendedor que cambia su producto o el cliente que cancela un pedido (repone stock)
    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR)
    @Override
    public List<String> clientesConFavorito(String productoId) {
        return listaDeseosRepository.clientesConFavorito(productoId);
    }
}
