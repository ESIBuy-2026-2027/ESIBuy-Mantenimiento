package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoWriteException;
import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.carrito.domain.model.Carrito;
import es.uclm.esibuy.backend.carrito.domain.model.LineaCarrito;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.time.Instant;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB del carrito (HU9). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class CarritoMongoAdapterTest {

    private static final int VALIDACION = 121; // "Document failed validation"
    private static final int CLAVE_DUPLICADA = 11000;
    private static final Instant AYER = Instant.parse("2026-10-28T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-10-29T10:00:00Z");

    @Autowired
    private CarritoMongoRepository carritoMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private CarritoMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        carritoMongoRepository.deleteAll();
        adapter = new CarritoMongoAdapter(carritoMongoRepository, new CarritoDocumentMapper());
    }

    private static Carrito carritoNuevo(String clienteId) {
        return new Carrito(null, clienteId, List.of(new LineaCarrito("p-1", 2, AYER, HOY)));
    }

    @Test
    @DisplayName("HU9-CP17: guardar un carrito nuevo le asigna un id y se recupera por su cliente")
    void guardarUnCarritoNuevoYRecuperarloPorCliente() {
        Carrito guardado = adapter.guardar(carritoNuevo("cliente-1"));

        assertNotNull(guardado.getId());
        Carrito leido = adapter.buscarPorCliente("cliente-1").orElseThrow();
        assertEquals(guardado.getId(), leido.getId());
        assertEquals(List.of(new LineaCarrito("p-1", 2, AYER, HOY)), leido.getLineas());
        assertTrue(adapter.buscarPorCliente("cliente-2").isEmpty());
    }

    @Test
    @DisplayName("HU9-CP22: eliminar por cliente borra su carrito de la colección y no el de otro cliente")
    void eliminarPorClienteBorraSoloSuCarrito() {
        adapter.guardar(carritoNuevo("cliente-1"));
        adapter.guardar(carritoNuevo("cliente-2"));

        adapter.eliminarPorCliente("cliente-1");

        assertTrue(adapter.buscarPorCliente("cliente-1").isEmpty());
        assertTrue(adapter.buscarPorCliente("cliente-2").isPresent());
    }

    @Test
    @DisplayName("HU9-CP17: guardar otra vez el carrito lo actualiza y conserva su fecha de creación")
    void guardarOtraVezActualizaElCarrito() {
        Carrito guardado = adapter.guardar(carritoNuevo("cliente-1"));
        Object creadoEn = mongoTemplate.getCollection("carritos").find().first().get("createdAt");
        assertNotNull(creadoEn);

        guardado.vaciar();
        adapter.guardar(guardado);

        assertEquals(1, carritoMongoRepository.count());
        Document actualizado = mongoTemplate.getCollection("carritos").find().first();
        assertEquals(creadoEn, actualizado.get("createdAt"));
        assertTrue(adapter.buscarPorCliente("cliente-1").orElseThrow().getLineas().isEmpty());
    }

    @Test
    @DisplayName("HU9-CP17: la colección solo admite un carrito por cliente")
    void laColeccionSoloAdmiteUnCarritoPorCliente() {
        adapter.guardar(carritoNuevo("cliente-1"));

        MongoWriteException error = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection("carritos").insertOne(new Document("clienteId", "cliente-1")));
        assertEquals(CLAVE_DUPLICADA, error.getCode());
    }

    @Test
    @DisplayName("HU9-CP17: la colección rechaza una línea con cantidad menor que 1")
    void laColeccionRechazaUnaCantidadMenorQueUno() {
        // Segunda barrera, a nivel de BBDD: aunque fallen las capas superiores
        Document conCantidadCero = new Document("clienteId", "cliente-2")
                .append("items", List.of(new Document("productoId", "p-1").append("cantidad", 0)));

        MongoWriteException error = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection("carritos").insertOne(conCantidadCero));
        assertEquals(VALIDACION, error.getCode());
    }
}
