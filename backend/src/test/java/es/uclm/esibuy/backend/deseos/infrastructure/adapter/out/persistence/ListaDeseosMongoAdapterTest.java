package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoWriteException;
import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
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

// Test de integración del adaptador de MongoDB de la lista de deseos (HU14). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class ListaDeseosMongoAdapterTest {

    private static final String COLECCION = "listas_deseos";
    private static final int CLAVE_DUPLICADA = 11000;
    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-11-04T10:00:00Z");

    @Autowired
    private ListaDeseosMongoRepository listaDeseosMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ListaDeseosMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        listaDeseosMongoRepository.deleteAll();
        adapter = new ListaDeseosMongoAdapter(listaDeseosMongoRepository, new ListaDeseosDocumentMapper());
    }

    private static ListaDeseos listaNueva(String clienteId) {
        return new ListaDeseos(null, clienteId, List.of(new ItemDeseo("p-1", AYER)));
    }

    @Test
    @DisplayName("HU14-CP06: guardar una lista nueva le asigna un id y se recupera por su cliente")
    void guardarUnaListaNuevaYRecuperarlaPorCliente() {
        ListaDeseos guardada = adapter.guardar(listaNueva("cliente-1"));

        assertNotNull(guardada.getId());
        ListaDeseos leida = adapter.buscarPorCliente("cliente-1").orElseThrow();
        assertEquals(guardada.getId(), leida.getId());
        assertEquals(List.of(new ItemDeseo("p-1", AYER)), leida.getItems());
        assertTrue(adapter.buscarPorCliente("cliente-2").isEmpty());
    }

    @Test
    @DisplayName("HU14-CP06: guardar otra vez la lista la actualiza sin crear otra y conserva su fecha de creación")
    void guardarOtraVezActualizaLaLista() {
        ListaDeseos guardada = adapter.guardar(listaNueva("cliente-1"));
        Object creadaEn = mongoTemplate.getCollection(COLECCION).find().first().get("createdAt");
        assertNotNull(creadaEn);

        guardada.anadir("p-2", HOY);
        adapter.guardar(guardada);

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        Document documento = mongoTemplate.getCollection(COLECCION).find().first();
        assertEquals(creadaEn, documento.get("createdAt"));
        assertEquals(2, adapter.buscarPorCliente("cliente-1").orElseThrow().getItems().size());
    }

    @Test
    @DisplayName("HU14-CP43: eliminar por cliente borra su lista de la colección y no la de otro cliente")
    void eliminarPorClienteBorraSoloSuLista() {
        adapter.guardar(listaNueva("cliente-1"));
        adapter.guardar(listaNueva("cliente-2"));

        adapter.eliminarPorCliente("cliente-1");

        assertTrue(adapter.buscarPorCliente("cliente-1").isEmpty());
        assertTrue(adapter.buscarPorCliente("cliente-2").isPresent());
    }

    @Test
    @DisplayName("HU14-CP06: la colección solo admite una lista por cliente")
    void laColeccionSoloAdmiteUnaListaPorCliente() {
        adapter.guardar(listaNueva("cliente-1"));

        MongoWriteException error = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection(COLECCION).insertOne(new Document("usuarioId", "cliente-1")));
        assertEquals(CLAVE_DUPLICADA, error.getCode());
    }
}
