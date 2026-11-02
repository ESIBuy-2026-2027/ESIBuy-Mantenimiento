package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface ValoracionMongoRepository extends MongoRepository<ValoracionDocument, String> {

    List<ValoracionDocument> findByProductoId(String productoId);

    boolean existsByUsuarioIdAndProductoId(String usuarioId, String productoId);
}
