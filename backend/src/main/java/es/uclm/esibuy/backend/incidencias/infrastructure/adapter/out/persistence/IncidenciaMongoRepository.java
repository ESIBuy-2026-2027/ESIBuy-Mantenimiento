package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;

public interface IncidenciaMongoRepository extends MongoRepository<IncidenciaDocument, String> {

    // El usuarioId es el autor de la incidencia; el índice por usuarioId lo crea InicializadorColecciones
    Page<IncidenciaDocument> findByUsuarioId(String usuarioId, Pageable pageable);

    // Sin resolver y con ese estado guardado (PENDIENTE, ABIERTA)
    Page<IncidenciaDocument> findByEstadoAndFechaResolucionIsNull(String estado, Pageable pageable);

    // Las resueltas (HU192): las que tienen fecha de resolución
    Page<IncidenciaDocument> findByFechaResolucionNotNull(Pageable pageable);

    Page<IncidenciaDocument> findByAsuntoContainingIgnoreCase(String asunto, Pageable pageable);

    Page<IncidenciaDocument> findByEstadoAndFechaResolucionIsNullAndAsuntoContainingIgnoreCase(String estado,
            String asunto, Pageable pageable);

    Page<IncidenciaDocument> findByFechaResolucionNotNullAndAsuntoContainingIgnoreCase(String asunto,
            Pageable pageable);

    // Pone la fecha de resolución solo si sigue abierta y sin resolver; devuelve cuántas ha cambiado (0 o 1)
    @Query("{ '_id': ?0, 'estado': 'ABIERTA', 'fechaResolucion': null }")
    @Update("{ '$set': { 'fechaResolucion': ?1 } }")
    long registrarResolucion(String id, Instant fechaResolucion);
}