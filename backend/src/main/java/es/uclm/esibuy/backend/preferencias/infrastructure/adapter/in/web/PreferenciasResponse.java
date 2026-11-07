package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import java.util.List;

/** Lo que la API devuelve de las preferencias: los identificadores de las categorías que interesan al cliente. */
public record PreferenciasResponse(List<String> categorias) {
}
