package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface AlertaMongoRepository extends MongoRepository<AlertaDocument, String> {

    // Usa el índice cliente_fecha de AlertaDocument
    List<AlertaDocument> findByClienteIdOrderByFechaDesc(String clienteId);

    // Por id y cliente a la vez: una alerta de otro cliente no se encuentra (AB#531)
    Optional<AlertaDocument> findByIdAndClienteId(String id, String clienteId);
}
