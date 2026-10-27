package es.uclm.esibuy.backend.categorias.application.port.in;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** Una fila del listado de categorías del administrador (HU39): la categoría y su número de productos. */
public record CategoriaConProductos(Categoria categoria, long numeroProductos) {
}
