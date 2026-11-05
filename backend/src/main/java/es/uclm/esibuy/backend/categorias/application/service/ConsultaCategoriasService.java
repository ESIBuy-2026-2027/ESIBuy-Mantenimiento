package es.uclm.esibuy.backend.categorias.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Implementa la consulta de categorías. Solo habla con el exterior a través del puerto del repositorio. */
public class ConsultaCategoriasService implements ConsultaCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;

    public ConsultaCategoriasService(CategoriaRepositoryPort categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @PreAuthorize(Permisos.PUBLICO)
    @Override
    public List<Categoria> listar() {
        return categoriaRepository.listar().stream()
        .filter(categoria -> categoria.estado() == EstadoCategoria.ACTIVA)
        .toList();
    }
}
