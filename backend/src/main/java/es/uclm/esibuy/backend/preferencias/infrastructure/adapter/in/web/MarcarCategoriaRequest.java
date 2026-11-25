package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

/**
 * Cuerpo de POST /api/customer/preferences/categories: la categoría que el cliente marca como favorita (AB#528). Que
 * exista y esté activa lo comprueba el caso de uso. El cliente sale de la sesión.
 */
public record MarcarCategoriaRequest(@NotBlank String categoriaId) {
}
