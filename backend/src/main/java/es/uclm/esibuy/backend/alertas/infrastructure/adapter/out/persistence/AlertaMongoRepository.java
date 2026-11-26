package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertaMongoRepository extends MongoRepository<AlertaDocument, String> {

    // Usa el índice cliente_fecha de AlertaDocument
    List<AlertaDocument> findByClienteIdOrderByFechaDesc(String clienteId);
}
