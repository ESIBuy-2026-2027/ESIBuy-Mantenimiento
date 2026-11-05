package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

/**
 * Cuerpo del cambio de estado de una categoría (HU41, T416). Como al crearla, el estado es texto y no el enum para
 * que un valor desconocido sea un 400 de validación y no un error de lectura del JSON.
 */
public record CambiarEstadoCategoriaRequest(
        @NotNull @Pattern(regexp = "ACTIVA|INACTIVA") String estado
) {

    EstadoCategoria aEstado() {
        return EstadoCategoria.valueOf(estado);
    }
}
