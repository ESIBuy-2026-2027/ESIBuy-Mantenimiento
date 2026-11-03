package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

/** Cuerpo de POST /api/customer/wishlist/items. Un clienteId en el cuerpo se ignora: el cliente sale de la sesión. */
public record AnadirDeseoRequest(@NotBlank String productoId) {
}
