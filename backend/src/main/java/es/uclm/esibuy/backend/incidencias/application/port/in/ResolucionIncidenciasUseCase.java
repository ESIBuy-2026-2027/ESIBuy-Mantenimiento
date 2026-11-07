package es.uclm.esibuy.backend.incidencias.application.port.in;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/**
 * Resolución de las incidencias por el administrador (HU50): abrirlas con su respuesta y cerrarlas. Es un puerto
 * distinto de {@link SupervisionIncidenciasUseCase} (HU49): el actor es el mismo, pero aquella solo consulta y esta
 * cambia las incidencias.
 */
public interface ResolucionIncidenciasUseCase {

    /**
     * Abre una incidencia pendiente con la respuesta del administrador, que es lo que verá su autor.
     *
     * @return la incidencia abierta, ya guardada
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException si no existe
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException si ya está abierta
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException si la respuesta falta o
     *         supera los 1000 caracteres
     */
    Incidencia abrir(String id, String respuesta);

    /**
     * Cierra una incidencia, pendiente o abierta: al cerrarla se elimina.
     *
     * @throws es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoEncontradaException si no existe
     */
    void cerrar(String id);
}
