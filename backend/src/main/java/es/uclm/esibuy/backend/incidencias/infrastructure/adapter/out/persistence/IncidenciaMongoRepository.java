package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface IncidenciaMongoRepository extends MongoRepository<IncidenciaDocument, String> {

    // El usuarioId es el autor de la incidencia; el índice por usuarioId lo crea InicializadorColecciones
    Page<IncidenciaDocument> findByUsuarioId(String usuarioId, Pageable pageable);
}