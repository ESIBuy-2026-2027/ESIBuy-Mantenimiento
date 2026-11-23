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
import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionDuplicadaException;
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
import org.springframework.data.mongodb.core.MongoTemplate;

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

    private Valoracion nueva(String productoId, String clienteId, int puntuacion, String comentario) {
        return Valoracion.nueva(productoId, clienteId, "Ana Pérez", puntuacion, comentario);
    }

    @Test
    @DisplayName("CLI-03: MongoDB asigna id y conserva autor y fecha")
    void guardaYRecuperaAutorYFecha() {
        Valoracion guardada = adapter.guardar(nueva("p-1", "u-1", 4, "Buena calidad"));
        Valoracion recuperada = adapter.buscarPorProducto("p-1").getFirst();

        assertNotNull(guardada.id());
        assertEquals("Ana Pérez", recuperada.nombreCliente());
        assertNotNull(recuperada.fechaCreacion());
        // BSON Date almacena precisión de milisegundos; Instant puede contener nanosegundos.
        assertEquals(guardada.fechaCreacion().toEpochMilli(), recuperada.fechaCreacion().toEpochMilli());
    }

    @Test
    @DisplayName("CLI-03: persiste el autor, fecha e id del cliente con los nombres del esquema")
    void persisteLosMetadatos() {
        adapter.guardar(nueva("p-1", "u-1", 4, "Buena calidad"));

        Document documento = mongoTemplate.getCollection("valoraciones").find().first();
        assertEquals("u-1", documento.get("usuarioId"));
        assertFalse(documento.containsKey("clienteId"));
        assertEquals("Ana Pérez", documento.getString("nombreCliente"));
        assertNotNull(documento.getDate("fechaCreacion"));
        assertEquals("p-1", documento.getString("productoId"));
        assertEquals(4, documento.getInteger("puntuacion"));
    }

    @Test
    @DisplayName("HU13-CP05: buscar por producto devuelve solo las valoraciones de ese producto")
    void buscarPorProductoDevuelveSoloLasDeEseProducto() {
        adapter.guardar(nueva("p-1", "u-1", 4, ""));
        adapter.guardar(nueva("p-1", "u-2", 2, ""));
        adapter.guardar(nueva("p-2", "u-1", 5, ""));

        List<Valoracion> delProducto = adapter.buscarPorProducto("p-1");

        assertEquals(2, delProducto.size());
        assertEquals(Set.of("u-1", "u-2"), delProducto.stream().map(Valoracion::clienteId).collect(toSet()));
        assertTrue(adapter.buscarPorProducto("p-3").isEmpty());
    }

    @Test
    @DisplayName("HU47-CP02: eliminar por producto borra sus valoraciones y conserva las demás")
    void eliminarPorProductoBorraSoloLasValoracionesDelProducto() {
        adapter.guardar(nueva("p-1", "u-1", 4, ""));
        adapter.guardar(nueva("p-2", "u-1", 5, ""));
        adapter.eliminarPorProducto("p-1");
        assertTrue(adapter.buscarPorProducto("p-1").isEmpty());
        assertEquals(1, adapter.buscarPorProducto("p-2").size());
    }

    @Test
    @DisplayName("HU13-CP05: sabe si un cliente ya ha valorado un producto")
    void sabeSiUnClienteYaHaValoradoUnProducto() {
        adapter.guardar(nueva("p-1", "u-1", 4, ""));
        assertTrue(adapter.existePorClienteYProducto("u-1", "p-1"));
        assertFalse(adapter.existePorClienteYProducto("u-1", "p-2"));
        assertFalse(adapter.existePorClienteYProducto("u-2", "p-1"));
    }

    @Test
    @DisplayName("HU13-CP05: una valoración por cliente y producto")
    void laColeccionSoloAdmiteUnaValoracionPorClienteYProducto() {
        adapter.guardar(nueva("p-1", "u-1", 4, ""));
        Valoracion repetida = nueva("p-1", "u-1", 2, "");
        assertThrows(ValoracionDuplicadaException.class, () -> adapter.guardar(repetida));
        assertEquals(1, valoracionMongoRepository.count());
    }
}
