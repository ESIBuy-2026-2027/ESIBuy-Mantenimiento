package es.uclm.esibuy.backend.incidencias.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
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

    // Las incidencias de todos los autores filtradas por estado, de las más antiguas a las más recientes
    IncidenciaPage buscarPorEstado(EstadoIncidencia estado, int pagina, int tamano);

    // Búsqueda paginada para supervisión con filtros opcionales de estado, asunto y ordenación
    IncidenciaPage buscarSupervisadas(EstadoIncidencia estado, String busqueda, String ordenarPor, String sentido,
            int pagina, int tamano);

    // La incidencia con ese id, o vacío si no existe
    Optional<Incidencia> buscarPorId(String id);

    // Borra la incidencia con ese id; si no existe, no hace nada
    void eliminar(String id);

    // Guarda la fecha de resolución solo si la incidencia sigue abierta, en una única operación: así no se sobrescribe
    // la de otro administrador ni se recrea una incidencia cerrada. Devuelve false si no ha cambiado nada
    boolean registrarResolucion(Incidencia resuelta);
}
