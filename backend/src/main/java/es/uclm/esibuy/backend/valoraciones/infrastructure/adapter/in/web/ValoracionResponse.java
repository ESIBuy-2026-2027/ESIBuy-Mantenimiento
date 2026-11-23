package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

import java.time.Instant;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Respuesta pública de una valoración, sin exponer el identificador interno del cliente. */
public record ValoracionResponse(String id, String productoId, int puntuacion, String comentario,
        String nombreCliente, Instant fechaCreacion) {

    public static ValoracionResponse desde(Valoracion valoracion) {
        return new ValoracionResponse(valoracion.id(), valoracion.productoId(), valoracion.puntuacion(),
                valoracion.comentario(), valoracion.nombreCliente(), valoracion.fechaCreacion());
    }
}
