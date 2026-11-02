package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;

/**
 * Cuerpo de la activación y desactivación de un producto (HU16). {@code Boolean} y no {@code boolean}: así un cuerpo
 * sin {@code disponible} da 400 en lugar de desactivar el producto sin avisar.
 */
public record DisponibilidadRequest(@NotNull Boolean disponible) {
}
