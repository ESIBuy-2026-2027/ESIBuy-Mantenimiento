package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import static java.util.stream.Collectors.toSet;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;
import java.util.List;
import java.util.Set;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB de las valoraciones (HU13, T369). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP05, propuesto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class ValoracionMongoAdapterTest {

    @Autowired
    private ValoracionMongoRepository valoracionMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ValoracionMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        valoracionMongoRepository.deleteAll();
        adapter = new ValoracionMongoAdapter(valoracionMongoRepository, new ValoracionDocumentMapper());
    }

    @Test
    @DisplayName("HU13-CP05: guardar una valoración nueva le asigna un id y se recupera por su producto")
    void guardarUnaValoracionNuevaYRecuperarlaPorProducto() {
        Valoracion guardada = adapter.guardar(Valoracion.nueva("p-1", "u-1", 4, "Buena calidad"));

        assertNotNull(guardada.id());
        assertEquals(List.of(guardada), adapter.buscarPorProducto("p-1"));
    }

    @Test
    @DisplayName("HU13-CP05: en la colección el cliente se guarda como usuarioId")
    void elClienteSeGuardaComoUsuarioId() {
        adapter.guardar(Valoracion.nueva("p-1", "u-1", 4, "Buena calidad"));

        Document documento = mongoTemplate.getCollection("valoraciones").find().first();
        assertEquals("u-1", documento.get("usuarioId"));
        assertFalse(documento.containsKey("clienteId"));
        assertEquals("p-1", documento.get("productoId"));
        assertEquals(4, documento.get("puntuacion"));
        assertEquals("Buena calidad", documento.get("comentario"));
    }

    @Test
    @DisplayName("HU13-CP05: buscar por producto devuelve solo las valoraciones de ese producto")
    void buscarPorProductoDevuelveSoloLasDeEseProducto() {
        adapter.guardar(Valoracion.nueva("p-1", "u-1", 4, ""));
        adapter.guardar(Valoracion.nueva("p-1", "u-2", 2, ""));
        adapter.guardar(Valoracion.nueva("p-2", "u-1", 5, ""));

        List<Valoracion> delProducto = adapter.buscarPorProducto("p-1");

        assertEquals(2, delProducto.size());
        assertEquals(Set.of("u-1", "u-2"), delProducto.stream().map(Valoracion::clienteId).collect(toSet()));
        assertTrue(adapter.buscarPorProducto("p-3").isEmpty());
    }

    @Test
    @DisplayName("HU13-CP05: sabe si un cliente ya ha valorado un producto")
    void sabeSiUnClienteYaHaValoradoUnProducto() {
        adapter.guardar(Valoracion.nueva("p-1", "u-1", 4, ""));

        assertTrue(adapter.existePorClienteYProducto("u-1", "p-1"));
        assertFalse(adapter.existePorClienteYProducto("u-1", "p-2"));
        assertFalse(adapter.existePorClienteYProducto("u-2", "p-1"));
    }

    @Test
    @DisplayName("HU13-CP05: la colección solo admite una valoración por cliente y producto")
    void laColeccionSoloAdmiteUnaValoracionPorClienteYProducto() {
        // Segunda barrera, a nivel de BBDD: cierra la carrera de dos valoraciones simultáneas del mismo cliente
        adapter.guardar(Valoracion.nueva("p-1", "u-1", 4, ""));

        assertThrows(DuplicateKeyException.class, () -> adapter.guardar(Valoracion.nueva("p-1", "u-1", 2, "")));
        assertEquals(1, valoracionMongoRepository.count());
    }
}
