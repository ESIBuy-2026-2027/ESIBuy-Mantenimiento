package es.uclm.esibuy.backend.deseos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Min;

/** Petición para cambiar la cantidad deseada de un producto en una lista de deseos (PBI 534). */
public record CantidadDeseoRequest(@Min(value = 1, message = "La cantidad mínima es 1") int cantidad) {
}
