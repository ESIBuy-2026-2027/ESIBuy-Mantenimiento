package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** DTO de salida de una fila del listado de categorías del administrador (HU39), con el estado como texto. */
public record CategoriaAdministracionResponse(String id, String nombre, String descripcion, String estado,
        long numeroProductos) {

    static CategoriaAdministracionResponse desde(CategoriaConProductos fila) {
        Categoria categoria = fila.categoria();
        return new CategoriaAdministracionResponse(categoria.id(), categoria.nombre(), categoria.descripcion(),
                categoria.estado().name(), fila.numeroProductos());
    }
}
