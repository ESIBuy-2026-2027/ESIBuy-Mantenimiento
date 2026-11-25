package es.uclm.esibuy.backend.deseos.application.port.out;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Persistencia de las listas de deseos de un cliente (HU14, PBI 534). */
public interface ListaDeseosRepositoryPort {

    Optional<ListaDeseos> buscarPorCliente(String clienteId);

    List<ListaDeseos> buscarTodasPorCliente(String clienteId);

    Optional<ListaDeseos> buscarPorIdYCliente(String id, String clienteId);

    // Devuelve la lista guardada: si era nueva, con el id que le ha asignado la base de datos
    ListaDeseos guardar(ListaDeseos lista);

    void eliminar(String id, String clienteId);

    // Si el cliente no tiene listas no hace nada
    void eliminarPorCliente(String clienteId);
}
