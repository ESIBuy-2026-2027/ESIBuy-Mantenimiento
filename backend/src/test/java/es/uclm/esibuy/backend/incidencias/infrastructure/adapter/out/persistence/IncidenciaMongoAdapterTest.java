package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.time.Instant;
import java.util.Date;

import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;

// Test de integración del adaptador de MongoDB de las incidencias (HU44, T291). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU44-CP07, propuesto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class IncidenciaMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-11-06T10:15:30Z");

    @Autowired
    private IncidenciaMongoRepository incidenciaMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private IncidenciaMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        incidenciaMongoRepository.deleteAll();
        adapter = new IncidenciaMongoAdapter(incidenciaMongoRepository, new IncidenciaDocumentMapper());
    }

    @Test
    @DisplayName("HU44-CP07: guardar una incidencia nueva le asigna un id y devuelve sus datos")
    void guardarUnaIncidenciaNuevaLeAsignaUnId() {
        Incidencia guardada = adapter.guardar(Incidencia.nueva("u-1", "No me llega el pedido", "Hace una semana", AHORA));

        assertNotNull(guardada.id());
        assertEquals("u-1", guardada.autorId());
        assertEquals("No me llega el pedido", guardada.asunto());
        assertEquals("Hace una semana", guardada.descripcion());
        assertEquals(EstadoIncidencia.PENDIENTE, guardada.estado());
        assertEquals("", guardada.respuesta());
        assertEquals(AHORA, guardada.fechaCreacion());
    }

    @Test
    @DisplayName("HU44-CP07: en la colección el autor se guarda como usuarioId y el estado como texto")
    void elAutorSeGuardaComoUsuarioId() {
        adapter.guardar(Incidencia.nueva("u-1", "No me llega el pedido", "Hace una semana", AHORA));

        Document documento = mongoTemplate.getCollection("incidencias").find().first();
        assertNotNull(documento);
        assertEquals("u-1", documento.get("usuarioId"));
        assertFalse(documento.containsKey("autorId"));
        assertEquals("No me llega el pedido", documento.get("asunto"));
        assertEquals("Hace una semana", documento.get("descripcion"));
        assertEquals("PENDIENTE", documento.get("estado"));
        assertEquals("", documento.get("respuesta"));
        assertEquals(Date.from(AHORA), documento.get("fechaCreacion"));
    }

    @Test
    @DisplayName("HU44-CP07: un mismo usuario puede guardar varias incidencias")
    void unUsuarioPuedeGuardarVariasIncidencias() {
        Incidencia primera = adapter.guardar(Incidencia.nueva("u-1", "Primera", "Descripción", AHORA));
        Incidencia segunda = adapter.guardar(Incidencia.nueva("u-1", "Segunda", "Descripción", AHORA));

        assertNotEquals(primera.id(), segunda.id());
        assertEquals(2, incidenciaMongoRepository.count());
    }
}