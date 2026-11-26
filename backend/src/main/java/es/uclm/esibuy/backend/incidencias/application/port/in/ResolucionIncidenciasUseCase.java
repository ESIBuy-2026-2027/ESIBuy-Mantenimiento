package es.uclm.esibuy.backend.incidencias.application.port.in;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/**
 * Resolución de las incidencias por el administrador: abrirlas con su respuesta y cerrarlas (HU50) y marcarlas como
 * resueltas (HU192). Es un puerto distinto de {@link SupervisionIncidenciasUseCase} (HU49): el actor es el mismo,
 * pero aquella solo consulta y esta cambia las incidencias.
 */
public interface ResolucionIncidenciasUseCase {

    /**
     * Abre una incidencia pendiente con la respuesta del administrador, que es lo que verá su autor.
     *
     * @return la incidencia abierta, ya guardada
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException si no existe
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException si ya está abierta
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaResueltaException si ya está resuelta
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException si la respuesta falta o
     *         supera los 1000 caracteres
     */
    Incidencia abrir(String id, String respuesta);

    /**
     * Marca como resuelta una incidencia abierta. La fecha de resolución la pone el sistema.
     *
     * @return la incidencia resuelta, ya guardada
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException si no existe
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoAbiertaException si está pendiente
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaResueltaException si ya está resuelta;
     *         conserva la fecha original
     */
    Incidencia resolver(String id);

    /**
     * Cierra una incidencia, en cualquier estado: al cerrarla se elimina.
     *
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException si no existe
     */
    void cerrar(String id);
}
