package es.uclm.esibuy.backend.deseos.application.port.out;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Persistencia de los favoritos (AB#528) y de las listas de deseos (HU14, PBI 534) de cada cliente. */
public interface ListaDeseosRepositoryPort {

    Optional<ListaDeseos> buscarFavoritos(String clienteId);

    // Sus listas, de la más antigua a la más reciente, sin los favoritos
    List<ListaDeseos> buscarListas(String clienteId);

    // Vacío si no existe, es de otro cliente o son los favoritos
    Optional<ListaDeseos> buscarLista(String id, String clienteId);

    // La lista por defecto del cliente. Si ya la tiene (p. ej. por dos lecturas a la vez), devuelve la que hay
    ListaDeseos crearListaPorDefecto(String clienteId);

    // Devuelve la lista guardada: si era nueva, con el id que le ha asignado la base de datos
    ListaDeseos guardar(ListaDeseos lista);

    void eliminar(String id, String clienteId);

    // Los favoritos y todas las listas. Si el cliente no tiene no hace nada
    void eliminarPorCliente(String clienteId);
}
