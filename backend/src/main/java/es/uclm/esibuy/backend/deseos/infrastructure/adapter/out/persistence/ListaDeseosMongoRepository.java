package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ListaDeseosMongoRepository extends MongoRepository<ListaDeseosDocument, String> {

    Optional<ListaDeseosDocument> findByUsuarioId(String usuarioId);

    void deleteByUsuarioId(String usuarioId);
}
