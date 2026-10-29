package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;

/**
 * Contador de los números de pedido en la colección contadores: un documento por año ({@code pedidos-2026}) cuyo
 * valor se incrementa en una sola operación de MongoDB, así que dos pedidos a la vez nunca reciben el mismo.
 */
public class ContadorPedidosMongoAdapter implements ContadorPedidosPort {

    private static final String COLECCION = "contadores";
    private static final String VALOR = "valor";

    private final MongoTemplate mongoTemplate;

    public ContadorPedidosMongoAdapter(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public long siguiente(int anio) {
        try {
            return incrementar(anio);
        } catch (DuplicateKeyException e) {
            // El primer pedido del año a la vez que otro: los dos intentan crear el documento y uno pierde.
            // Al repetir, el documento ya existe y solo se incrementa.
            return incrementar(anio);
        }
    }

    private long incrementar(int anio) {
        Document contador = mongoTemplate.findAndModify(
                new Query(Criteria.where("_id").is("pedidos-" + anio)),
                new Update().inc(VALOR, 1L),
                FindAndModifyOptions.options().upsert(true).returnNew(true),
                Document.class, COLECCION);
        return ((Number) contador.get(VALOR)).longValue();
    }
}
