package es.uclm.esibuy.backend.incidencias.application.port.out;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/** Persistencia de las incidencias. */
public interface IncidenciaRepositoryPort {

    // Devuelve la incidencia guardada: si era nueva, con el id que le ha asignado la base de datos
    Incidencia guardar(Incidencia incidencia);

    // Las incidencias de un autor, de las más antiguas a las más recientes, por páginas (la primera es la 0)
    IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano);

    // Las incidencias de todos los autores, de las más antiguas a las más recientes, por páginas (la primera es la 0)
    IncidenciaPage buscarTodas(int pagina, int tamano);
}
