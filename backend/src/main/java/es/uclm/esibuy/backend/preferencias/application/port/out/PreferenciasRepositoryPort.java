package es.uclm.esibuy.backend.preferencias.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

/** Persistencia de las preferencias: unas por cliente. */
public interface PreferenciasRepositoryPort {

    Optional<Preferencias> buscarPorCliente(String clienteId);

    // Devuelve las preferencias guardadas: si eran nuevas, con el id que les ha asignado la base de datos
    Preferencias guardar(Preferencias preferencias);

    void eliminarPorCliente(String clienteId);
}
