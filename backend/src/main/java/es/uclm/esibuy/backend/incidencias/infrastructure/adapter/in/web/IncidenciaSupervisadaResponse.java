package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisada;

/**
 * Lo que la API devuelve de una incidencia al administrador (HU49). No lleva el id interno del autor, sino su correo
 * y su rol; {@code autor} es {@code null} si la cuenta se ha eliminado.
 */
public record IncidenciaSupervisadaResponse(String id, String asunto, String descripcion, String estado,
        String respuesta, Instant fechaCreacion, AutorResponse autor) {

    public record AutorResponse(String email, String rol) {
    }

    public static IncidenciaSupervisadaResponse desde(IncidenciaSupervisada supervisada) {
        Incidencia incidencia = supervisada.incidencia();
        AutorIncidencia autor = supervisada.autor();
        return new IncidenciaSupervisadaResponse(incidencia.id(), incidencia.asunto(), incidencia.descripcion(),
                incidencia.estado().name(), incidencia.respuesta(), incidencia.fechaCreacion(),
                autor == null ? null : new AutorResponse(autor.email(), autor.rol()));
    }
}
