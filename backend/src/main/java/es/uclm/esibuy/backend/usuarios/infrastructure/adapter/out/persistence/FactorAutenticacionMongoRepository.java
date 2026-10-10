package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface FactorAutenticacionMongoRepository extends MongoRepository<FactorAutenticacionDocument, String> {
    Optional<FactorAutenticacionDocument> findByUsuarioId(String usuarioId);
    void deleteByUsuarioId(String usuarioId);
}