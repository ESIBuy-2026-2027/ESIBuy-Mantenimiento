package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface CarritoMongoRepository extends MongoRepository<CarritoDocument, String> {

    Optional<CarritoDocument> findByClienteId(String clienteId);

    void deleteByClienteId(String clienteId);
}
