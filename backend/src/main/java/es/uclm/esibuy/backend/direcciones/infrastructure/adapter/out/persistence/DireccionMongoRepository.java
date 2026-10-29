package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DireccionMongoRepository extends MongoRepository<DireccionDocument, String> {

    List<DireccionDocument> findByClienteId(String clienteId);
}
