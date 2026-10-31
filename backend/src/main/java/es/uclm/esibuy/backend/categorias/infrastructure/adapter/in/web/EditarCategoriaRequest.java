package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import jakarta.validation.constraints.NotBlank;

import es.uclm.esibuy.backend.categorias.application.port.in.EditarCategoriaCommand;

/**
 * Cuerpo de la edición de una categoría (HU41, T328). El id va en la ruta y el estado no se edita. Los límites de
 * longitud los valida el dominio (CategoriaInvalidaException → 400).
 */
public record EditarCategoriaRequest(
        @NotBlank String nombre,
        String descripcion
) {

    EditarCategoriaCommand aComando(String id) {
        return new EditarCategoriaCommand(id, nombre, descripcion);
    }
}