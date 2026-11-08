package es.uclm.esibuy.backend.incidencias.application.port.in;

import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisadaPage;

/** Supervisión de las incidencias por el administrador (HU49). */
public interface SupervisionIncidenciasUseCase {

    /**
     * Todas las incidencias, de todos los autores, de la más antigua a la más reciente, por páginas. Cada una va con
     * el correo y el rol de quien la reportó, o sin autor si su cuenta se ha eliminado.
     *
     * @param pagina la página que se pide; la primera es la 0
     * @param tamano cuántas incidencias por página, de 1 a 50
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException si la página o el
     *         tamaño no son válidos
     */
    IncidenciaSupervisadaPage consultarTodas(int pagina, int tamano);
}
