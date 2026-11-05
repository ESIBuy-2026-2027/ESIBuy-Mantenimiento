package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CategoriaMongoRepository extends MongoRepository<CategoriaDocument, String> {
}
