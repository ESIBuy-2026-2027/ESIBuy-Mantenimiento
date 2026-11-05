package es.uclm.esibuy.backend.carrito.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** Cuerpo de POST /api/customer/cart/items. Un clienteId en el cuerpo se ignora: el cliente sale de la sesión. */
public record AnadirAlCarritoRequest(@NotBlank String productoId, @NotNull Integer cantidad) {
}
