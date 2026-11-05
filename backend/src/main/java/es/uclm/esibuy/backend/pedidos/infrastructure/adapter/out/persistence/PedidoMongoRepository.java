package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface PedidoMongoRepository extends MongoRepository<PedidoDocument, String> {

    boolean existsByClienteIdAndEstadoAndLineasProductoId(String clienteId, String estado, String productoId);

    // Historial del cliente (HU11): Spring Data genera la consulta y el orden a partir del nombre
    List<PedidoDocument> findByClienteIdOrderByCreatedAtDesc(String clienteId);

    List<PedidoDocument> findByVendedorId(String vendedorId);
}
