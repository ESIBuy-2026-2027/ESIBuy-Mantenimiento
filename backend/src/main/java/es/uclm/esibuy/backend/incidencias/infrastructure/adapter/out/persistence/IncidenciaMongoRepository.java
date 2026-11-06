package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidenciaMongoRepository extends MongoRepository<IncidenciaDocument, String> {
}