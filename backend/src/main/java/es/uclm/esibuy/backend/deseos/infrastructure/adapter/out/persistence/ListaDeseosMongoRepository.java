package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ListaDeseosMongoRepository extends MongoRepository<ListaDeseosDocument, String> {

    List<ListaDeseosDocument> findAllByUsuarioIdOrderByCreatedAtAsc(String usuarioId);

    Optional<ListaDeseosDocument> findByIdAndUsuarioId(String id, String usuarioId);

    void deleteByIdAndUsuarioId(String id, String usuarioId);

    void deleteByUsuarioId(String usuarioId);
}
