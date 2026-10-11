package es.uclm.esibuy.backend.categorias.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/**
 * Puerto de entrada de la consulta de las categorías del catálogo. Lo usan los formularios que eligen una
 * categoría (registro y edición de un vendedor, alta de un producto) y las pantallas que traducen el
 * identificador de una categoría a su nombre.
 */
public interface ConsultaCategoriasUseCase {

    /** Devuelve todas las categorías, ordenadas por nombre. */
    List<Categoria> listar();
}
