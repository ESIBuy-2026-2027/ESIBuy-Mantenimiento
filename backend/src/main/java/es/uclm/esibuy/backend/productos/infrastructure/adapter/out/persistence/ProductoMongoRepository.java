package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ProductoMongoRepository extends MongoRepository<ProductoDocument, String> {
}
