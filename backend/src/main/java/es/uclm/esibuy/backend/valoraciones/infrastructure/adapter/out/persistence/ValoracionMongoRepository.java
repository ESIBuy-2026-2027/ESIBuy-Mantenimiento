package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import java.time.Instant;
import java.util.List;

import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ValoracionMongoRepository extends MongoRepository<ValoracionDocument, String> {

    List<ValoracionDocument> findByProductoId(String productoId);

    boolean existsByUsuarioIdAndProductoId(String usuarioId, String productoId);

    void deleteByProductoId(String productoId);

    @Aggregation(pipeline = {
            "{ '$match': { 'productoId': { '$in': ?0 } } }",
            "{ '$group': { '_id': { 'productoId': '$productoId', 'puntuacion': '$puntuacion' }, 'numero': { '$sum': 1 } } }",
            "{ '$project': { '_id': 0, 'productoId': '$_id.productoId', 'puntuacion': '$_id.puntuacion', 'numero': 1 } }"
    })
    List<ValoracionAgrupada> resumirPorProductos(List<String> productoIds);

    List<ValoracionDocument> findByProductoIdInAndFechaCreacionGreaterThanEqualAndFechaCreacionLessThan(
            List<String> productoIds, Instant desde, Instant hasta);

    interface ValoracionAgrupada {
        String getProductoId();
        int getPuntuacion();
        long getNumero();
    }
}
