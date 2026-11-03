package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;

/** Lo que la API devuelve al añadir a la lista de deseos: qué productos tiene y cuándo se añadieron. */
public record ListaDeseosResponse(List<Item> items) {

    public record Item(String productoId, Instant addedAt) {
    }

    public static ListaDeseosResponse desde(ListaDeseos lista) {
        return new ListaDeseosResponse(List.of());
    }
}
