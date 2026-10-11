package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.domain.Sort;

import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** Adaptador de salida que implementa el repositorio de categorías con Spring Data MongoDB. */
public class CategoriaMongoAdapter implements CategoriaRepositoryPort {

    private final CategoriaMongoRepository repository;

    public CategoriaMongoAdapter(CategoriaMongoRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Categoria> listar() {
        return repository.findAll(Sort.by("nombre")).stream()
                .map(documento -> new Categoria(documento.getId(), documento.getNombre()))
                .toList();
    }
}
