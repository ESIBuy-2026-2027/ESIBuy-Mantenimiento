package es.uclm.esibuy.backend.carrito.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;

/** Cuerpo de PUT /api/customer/cart/items/{productoId}: la cantidad nueva, que sustituye a la anterior. */
public record CantidadRequest(@NotNull Integer cantidad) {
}
