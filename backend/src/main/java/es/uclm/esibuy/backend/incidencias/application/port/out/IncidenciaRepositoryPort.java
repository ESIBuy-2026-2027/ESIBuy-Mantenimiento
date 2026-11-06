package es.uclm.esibuy.backend.incidencias.application.port.out;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Persistencia de las incidencias. */
public interface IncidenciaRepositoryPort {

    // Devuelve la incidencia guardada: si era nueva, con el id que le ha asignado la base de datos
    Incidencia guardar(Incidencia incidencia);
}