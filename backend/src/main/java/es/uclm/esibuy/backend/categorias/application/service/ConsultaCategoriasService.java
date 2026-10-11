package es.uclm.esibuy.backend.categorias.application.service;

import java.util.List;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** Implementa la consulta de categorías. Solo habla con el exterior a través del puerto del repositorio. */
public class ConsultaCategoriasService implements ConsultaCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    public ConsultaCategoriasService(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    public List<Categoria> listar() {
        return categoriaRepository.listar();
    }
}
