package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;

/** Lo que la API devuelve al consultar la lista de deseos: cada producto con sus datos actuales. */
public record ListaDeseosDetalladaResponse(List<Item> items) {

    public record Item(String productoId, String nombre, String imagen, BigDecimal precioFinal, int stock,
            boolean disponible, Instant addedAt) {
    }

    public static ListaDeseosDetalladaResponse desde(ListaDeseosDetallada lista) {
        return new ListaDeseosDetalladaResponse(lista.items().stream()
                .map(item -> new Item(item.productoId(), item.nombre(), item.imagen(), item.precioFinal(),
                        item.stock(), item.disponible(), item.addedAt()))
                .toList());
    }
}
