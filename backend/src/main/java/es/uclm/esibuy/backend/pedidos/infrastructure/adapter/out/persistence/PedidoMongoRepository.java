package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PedidoMongoRepository extends MongoRepository<PedidoDocument, String> {

    boolean existsByClienteIdAndEstadoAndLineasProductoId(String clienteId, String estado, String productoId);
}