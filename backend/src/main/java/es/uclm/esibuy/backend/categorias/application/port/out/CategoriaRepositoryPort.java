package es.uclm.esibuy.backend.categorias.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

public interface CategoriaRepositoryPort {

    // Todas las categorías, ordenadas por nombre.
    List<Categoria> listar();

    // ¿Hay ya una categoría con ese nombre? No distingue mayúsculas ni tildes (HU40).
    boolean existePorNombre(String nombre);

    // Devuelve la categoría guardada: si era nueva, con el id que le ha asignado la base de datos.
    Categoria guardar(Categoria categoria);
}
