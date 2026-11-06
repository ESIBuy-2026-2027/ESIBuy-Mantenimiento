package es.uclm.esibuy.backend.incidencias.domain.model;

import java.time.Instant;

/** Esqueleto del ciclo rojo: todavía no valida nada ni rellena los valores por defecto. */
public record Incidencia(String id, String autorId, String asunto, String descripcion, EstadoIncidencia estado,
        String respuesta, Instant fechaCreacion) {

    public static Incidencia nueva(String autorId, String asunto, String descripcion, Instant fechaCreacion) {
        return new Incidencia(null, autorId, asunto, descripcion, null, null, fechaCreacion);
    }
}