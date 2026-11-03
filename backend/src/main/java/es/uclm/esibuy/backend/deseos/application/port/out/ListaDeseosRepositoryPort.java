package es.uclm.esibuy.backend.deseos.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Persistencia de las listas de deseos: una por cliente. */
public interface ListaDeseosRepositoryPort {

    Optional<ListaDeseos> buscarPorCliente(String clienteId);

    // Devuelve la lista guardada: si era nueva, con el id que le ha asignado la base de datos
    ListaDeseos guardar(ListaDeseos lista);
}
