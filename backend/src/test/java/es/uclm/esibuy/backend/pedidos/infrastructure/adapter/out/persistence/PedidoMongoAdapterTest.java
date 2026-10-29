package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.math.BigDecimal;
import org.bson.Document;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB de pedidos (HU10). Usa un MongoDB real en Docker (Testcontainers):
// Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su validación e índices.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class PedidoMongoAdapterTest {

    @Autowired
    private PedidoMongoRepository pedidoMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private PedidoMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        pedidoMongoRepository.deleteAll();
        adapter = new PedidoMongoAdapter(pedidoMongoRepository, new PedidoDocumentMapper());
    }

    @Test
    @DisplayName("HU10-CP19: guardar un pedido le asigna un id y lo deja en la colección pedidos")
    void guardarUnPedidoLeAsignaUnId() {
        Pedido guardado = adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001"));

        assertNotNull(guardado.getId());
        Document enMongo = mongoTemplate.getCollection("pedidos").find().first();
        assertNotNull(enMongo);
        assertEquals("ESB-2026-000001", enMongo.get("numeroPedido"));
        assertEquals("c-1", enMongo.get("clienteId"));
        assertEquals("PAGADO", enMongo.get("estado"));
        assertEquals("mp-1", enMongo.get("paymentMethodId"));
        // Los importes van como Decimal128: el validador de la colección exige un número
        assertEquals(new Decimal128(new BigDecimal("197.32")), enMongo.get("total"));
        assertEquals("Calle Mayor 1", enMongo.get("direccionEnvio", Document.class).get("calle"));
    }

    @Test
    @DisplayName("HU10-CP19: dos pedidos no pueden tener el mismo número")
    void dosPedidosNoPuedenTenerElMismoNumero() {
        adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001"));

        assertThrows(DuplicateKeyException.class,
                () -> adapter.guardar(PedidoDocumentMapperTest.pedido(null, "ESB-2026-000001")));
    }
}
