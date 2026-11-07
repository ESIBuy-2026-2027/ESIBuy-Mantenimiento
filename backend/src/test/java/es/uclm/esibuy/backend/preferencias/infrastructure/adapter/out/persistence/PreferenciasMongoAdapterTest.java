package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoWriteException;
import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB de las preferencias (HU46, T287). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU46-CP12, propuesto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class PreferenciasMongoAdapterTest {

    private static final String COLECCION = "preferencias";
    private static final int CLAVE_DUPLICADA = 11000;
    private static final int VALIDACION_FALLIDA = 121;

    @Autowired
    private PreferenciasMongoRepository preferenciasMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private PreferenciasMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        preferenciasMongoRepository.deleteAll();
        adapter = new PreferenciasMongoAdapter(preferenciasMongoRepository, new PreferenciasDocumentMapper());
    }

    @Test
    @DisplayName("HU46-CP12: guardar unas preferencias nuevas les asigna un id y se recuperan por su cliente")
    void guardarPreferenciasNuevasYRecuperarlasPorCliente() {
        Preferencias guardadas = adapter.guardar(new Preferencias(null, "cliente-1", List.of("cat-1")));

        assertNotNull(guardadas.getId());
        Preferencias leidas = adapter.buscarPorCliente("cliente-1").orElseThrow();
        assertEquals(guardadas.getId(), leidas.getId());
        assertEquals(List.of("cat-1"), leidas.getCategorias());
        assertTrue(adapter.buscarPorCliente("cliente-2").isEmpty());
    }

    @Test
    @DisplayName("HU46-CP24: eliminar por cliente borra sus preferencias de la colección y no las de otro cliente")
    void eliminarPorClienteBorraSoloSusPreferencias() {
        adapter.guardar(new Preferencias(null, "cliente-1", List.of("cat-1")));
        adapter.guardar(new Preferencias(null, "cliente-2", List.of("cat-2")));

        adapter.eliminarPorCliente("cliente-1");

        assertTrue(adapter.buscarPorCliente("cliente-1").isEmpty());
        assertTrue(adapter.buscarPorCliente("cliente-2").isPresent());
    }

    @Test
    @DisplayName("HU46-CP12: guardar otra vez las preferencias las actualiza sin crear otras y conserva su fecha de creación")
    void guardarOtraVezActualizaLasPreferencias() {
        Preferencias guardadas = adapter.guardar(new Preferencias(null, "cliente-1", List.of("cat-1")));
        Object creadasEn = mongoTemplate.getCollection(COLECCION).find().first().get("createdAt");
        assertNotNull(creadasEn);

        guardadas.elegirCategorias(List.of("cat-2", "cat-3"));
        adapter.guardar(guardadas);

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        assertEquals(creadasEn, mongoTemplate.getCollection(COLECCION).find().first().get("createdAt"));
        assertEquals(List.of("cat-2", "cat-3"), adapter.buscarPorCliente("cliente-1").orElseThrow().getCategorias());
    }

    @Test
    @DisplayName("HU46-CP12: la colección solo admite unas preferencias por cliente y exige el cliente")
    void laColeccionSoloAdmiteUnasPreferenciasPorCliente() {
        adapter.guardar(new Preferencias(null, "cliente-1", List.of()));

        MongoWriteException duplicada = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection(COLECCION).insertOne(new Document("clienteId", "cliente-1")));
        assertEquals(CLAVE_DUPLICADA, duplicada.getCode());
        MongoWriteException sinCliente = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection(COLECCION).insertOne(new Document("categorias", List.of("cat-1"))));
        assertEquals(VALIDACION_FALLIDA, sinCliente.getCode());
    }
}
