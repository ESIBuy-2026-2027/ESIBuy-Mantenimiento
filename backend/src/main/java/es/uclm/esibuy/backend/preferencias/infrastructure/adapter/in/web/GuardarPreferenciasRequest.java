package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import java.util.List;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de PUT /api/customer/preferences: los identificadores de las categorías que interesan al cliente. Una lista
 * vacía quita todas; que cada una exista y esté activa lo comprueba el caso de uso. El cliente sale de la sesión.
 */
public record GuardarPreferenciasRequest(@NotNull List<String> categorias) {
}
