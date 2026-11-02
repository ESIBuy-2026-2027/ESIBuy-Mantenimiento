package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Collation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** Adaptador de salida que implementa el repositorio de categorías con Spring Data MongoDB. */
public class CategoriaMongoAdapter implements CategoriaRepositoryPort {

    // La misma collation que el índice nombre_sin_mayusculas (ColeccionesEsiBuy): nivel 1, sin distinguir
    // mayúsculas ni tildes. La «ñ» sí es otra letra en español.
    private static final Collation SIN_MAYUSCULAS_NI_TILDES =
            Collation.of("es").strength(Collation.ComparisonLevel.primary());

    private final CategoriaMongoRepository repository;
    private final CategoriaDocumentMapper mapper;
    private final MongoTemplate mongoTemplate;

    public CategoriaMongoAdapter(CategoriaMongoRepository repository, CategoriaDocumentMapper mapper,
            MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mapper = mapper;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<Categoria> listar() {
        return repository.findAll(Sort.by("nombre")).stream()
                .map(mapper::aDominio)
                .toList();
    }

    // Igualdad con la collation, no una expresión regular: el nombre se compara completo y como texto literal.
    @Override
    public boolean existePorNombre(String nombre) {
        Query consulta = new Query(Criteria.where("nombre").is(nombre.strip())).collation(SIN_MAYUSCULAS_NI_TILDES);
        return mongoTemplate.exists(consulta, CategoriaDocument.class);
    }

    @Override
    public Categoria guardar(Categoria categoria) {
        try {
            return mapper.aDominio(repository.save(mapper.aDocumento(categoria)));
        } catch (DuplicateKeyException duplicateKeyException) {
            // El índice único nombre_sin_mayusculas lo ha rechazado: dos altas simultáneas con el mismo nombre.
            throw new CategoriaDuplicadaException(duplicateKeyException);
        }
    }

    // Busca una categoría por su ID.
    @Override
    public Optional<Categoria> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    // Igual que existePorNombre, pero sin contar la categoría con ese id.
    @Override
    public boolean existeOtraConNombre(String nombre, String idExcluido) {
        Query consulta = new Query(Criteria.where("nombre").is(nombre.strip()).and("id").ne(idExcluido))
                .collation(SIN_MAYUSCULAS_NI_TILDES);
        return mongoTemplate.exists(consulta, CategoriaDocument.class);
    }

    // Esqueleto del ciclo rojo (HU42, T326): se implementa en el ciclo del adaptador.
    @Override
    public void eliminar(String id) {
    }
}
