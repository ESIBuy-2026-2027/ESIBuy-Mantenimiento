package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence;

import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface PreferenciasMongoRepository extends MongoRepository<PreferenciasDocument, String> {

    Optional<PreferenciasDocument> findByClienteId(String clienteId);
}
