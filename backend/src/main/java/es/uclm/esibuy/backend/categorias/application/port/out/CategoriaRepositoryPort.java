package es.uclm.esibuy.backend.categorias.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

public interface CategoriaRepositoryPort {

    // Todas las categorías, ordenadas por nombre.
    List<Categoria> listar();
}
