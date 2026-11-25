package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import org.bson.Document;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Updates;

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
        Document contador = mongoTemplate.getCollection(COLECCION).findOneAndUpdate(
                Filters.eq("_id", "pedidos-" + anio),
                Updates.inc(VALOR, 1L),
                new FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER));
        return contador.getLong(VALOR);
    }
}
