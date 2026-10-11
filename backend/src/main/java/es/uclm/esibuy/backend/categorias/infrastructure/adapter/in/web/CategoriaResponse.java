package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** DTO de salida de una categoría, con el formato que esperan los formularios del frontend. */
public record CategoriaResponse(String id, String nombre) {

    static CategoriaResponse desde(Categoria categoria) {
        return new CategoriaResponse(categoria.id(), categoria.nombre());
    }
}
