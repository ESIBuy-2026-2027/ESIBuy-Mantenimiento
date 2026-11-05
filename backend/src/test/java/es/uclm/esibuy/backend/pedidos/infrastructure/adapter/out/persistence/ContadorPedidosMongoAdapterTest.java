package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del contador de números de pedido (HU10), en la colección contadores de un MongoDB real
// (Testcontainers): Docker Desktop debe estar abierto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class ContadorPedidosMongoAdapterTest {

    @Autowired
    private MongoTemplate mongoTemplate;

    private ContadorPedidosMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        mongoTemplate.getCollection("contadores").deleteMany(new Document());
        adapter = new ContadorPedidosMongoAdapter(mongoTemplate);
    }

    @Test
    @DisplayName("HU10-CP13: el contador de un año empieza en 1 y sube de uno en uno")
    void elContadorEmpiezaEnUnoYSube() {
        assertEquals(1, adapter.siguiente(2026));
        assertEquals(2, adapter.siguiente(2026));
        assertEquals(3, adapter.siguiente(2026));
    }

    @Test
    @DisplayName("HU10-CP13: cada año tiene su propio contador")
    void cadaAnoTieneSuPropioContador() {
        adapter.siguiente(2026);
        adapter.siguiente(2026);

        assertEquals(1, adapter.siguiente(2027));
        assertEquals(3, adapter.siguiente(2026));
    }

    @Test
    @DisplayName("HU10-CP13: dos pedidos a la vez nunca reciben el mismo número")
    void dosPedidosALaVezNoRecibenElMismoNumero() throws Exception {
        List<Callable<Long>> peticiones = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            peticiones.add(() -> adapter.siguiente(2026));
        }
        ExecutorService hilos = Executors.newFixedThreadPool(8);
        List<Long> numeros = new ArrayList<>();
        for (Future<Long> peticion : hilos.invokeAll(peticiones)) {
            numeros.add(peticion.get());
        }
        hilos.shutdown();

        assertEquals(20, new HashSet<>(numeros).size());
        assertEquals(20L, numeros.stream().mapToLong(Long::longValue).max().orElseThrow());
    }
}
