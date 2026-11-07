package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Lo que la API devuelve de una incidencia. No lleva el autor: no se expone su id interno. */
public record IncidenciaResponse(String id, String asunto, String descripcion, String estado, String respuesta,
        Instant fechaCreacion) {

    public static IncidenciaResponse desde(Incidencia incidencia) {
        return new IncidenciaResponse(incidencia.id(), incidencia.asunto(), incidencia.descripcion(),
                incidencia.estado().name(), incidencia.respuesta(), incidencia.fechaCreacion());
    }
}