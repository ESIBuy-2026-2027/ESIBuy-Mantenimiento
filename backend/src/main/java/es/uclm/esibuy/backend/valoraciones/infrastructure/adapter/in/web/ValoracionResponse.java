package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/** Lo que la API devuelve de una valoración. No lleva el cliente: no se expone su id interno. */
public record ValoracionResponse(String id, String productoId, int puntuacion, String comentario) {

    public static ValoracionResponse desde(Valoracion valoracion) {
        return new ValoracionResponse(valoracion.id(), valoracion.productoId(), valoracion.puntuacion(),
                valoracion.comentario());
    }
}
