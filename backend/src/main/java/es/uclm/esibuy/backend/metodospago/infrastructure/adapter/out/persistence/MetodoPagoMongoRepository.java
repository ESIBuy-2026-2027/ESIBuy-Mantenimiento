package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface MetodoPagoMongoRepository extends MongoRepository<MetodoPagoDocument, String> {

    List<MetodoPagoDocument> findByClienteId(String clienteId);

    void deleteByClienteId(String clienteId);
}
