package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

/**
 * Cuerpo de la creación de una categoría (HU40, T322). El estado es texto y no el enum para que un valor
 * desconocido sea un 400 de validación y no un error de lectura del JSON. Los límites de longitud los valida
 * el dominio (CategoriaInvalidaException → 400).
 */
public record CrearCategoriaRequest(
        @NotBlank String nombre,
        String descripcion,
        @NotNull @Pattern(regexp = "ACTIVA|INACTIVA") String estado
) {

    CrearCategoriaCommand aComando() {
        return new CrearCategoriaCommand(nombre, descripcion, EstadoCategoria.valueOf(estado));
    }
}
