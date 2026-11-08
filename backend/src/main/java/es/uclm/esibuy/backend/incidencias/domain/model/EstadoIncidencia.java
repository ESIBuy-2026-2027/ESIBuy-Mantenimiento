package es.uclm.esibuy.backend.incidencias.domain.model;

/**
 * Estado de una incidencia (HU44). Nace {@code PENDIENTE}, sin que ningún administrador la haya revisado, y pasa a
 * {@code ABIERTA} cuando uno la abre (HU50). No hay estado cerrada: una incidencia cerrada se elimina.
 */
public enum EstadoIncidencia {
    PENDIENTE,
    ABIERTA
}