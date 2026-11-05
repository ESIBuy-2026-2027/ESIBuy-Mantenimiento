package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;

import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

/**
 * Cuerpo del cambio de estado de una categoría (HU41, T416). Un estado que no es ACTIVA ni INACTIVA no se puede leer
 * del JSON y, como uno que falta, responde 400 VALIDATION_ERROR (ManejadorGlobalErrores).
 */
public record CambiarEstadoCategoriaRequest(
        @NotNull EstadoCategoria estado
) {
}
