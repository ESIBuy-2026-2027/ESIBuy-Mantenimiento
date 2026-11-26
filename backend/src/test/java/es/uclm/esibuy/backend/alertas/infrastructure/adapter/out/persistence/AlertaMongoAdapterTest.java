package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
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

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;

// Test de integración del adaptador de MongoDB de las alertas (AB#529). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP15, propuesto.
@DataMongoTest
@Import({ MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class })
class AlertaMongoAdapterTest {

    private static final Instant AYER = Instant.parse("2026-11-25T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-11-26T10:00:00Z");

    @Autowired
    private AlertaMongoRepository alertaMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private AlertaMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        alertaMongoRepository.deleteAll();
        adapter = new AlertaMongoAdapter(alertaMongoRepository, new AlertaDocumentMapper());
    }

    @Test
    @DisplayName("HU529-CP15: guarda varias alertas a la vez y cada cliente recupera solo las suyas, de la más reciente a la más antigua")
    void guardaVariasYCadaClienteRecuperaLasSuyas() {
        adapter.guardarTodas(List.of(
                Alerta.disponibilidad("c-1", "p-1", "Teclado", "https://img.test/p.png", AYER),
                Alerta.bajadaDePrecio("c-1", "p-2", "Ratón", "https://img.test/p.png", new BigDecimal("20.00"), new BigDecimal("15.50"), HOY),
                Alerta.disponibilidad("c-2", "p-1", "Teclado", "https://img.test/p.png", HOY)));

        List<Alerta> delCliente = adapter.buscarPorCliente("c-1");

        assertEquals(2, delCliente.size());
        Alerta reciente = delCliente.get(0);
        assertNotNull(reciente.getId());
        assertEquals(TipoAlerta.BAJADA_PRECIO, reciente.getTipo());
        assertEquals(HOY, reciente.getFecha());
        assertEquals(0, new BigDecimal("20.00").compareTo(reciente.getDetalle().precioAnterior()));
        assertEquals(0, new BigDecimal("15.50").compareTo(reciente.getDetalle().precioNuevo()));
        assertEquals(TipoAlerta.DISPONIBILIDAD, delCliente.get(1).getTipo());
        assertEquals(1, adapter.buscarPorCliente("c-2").size());
        assertTrue(adapter.buscarPorCliente("c-3").isEmpty());
    }

    @Test
    @DisplayName("HU529-CP15: la alerta se guarda con tipo, cliente, producto, fecha, leída y fecha de creación, y los precios como número")
    void guardaLosCamposDelEsquema() {
        adapter.guardarTodas(List.of(
                Alerta.bajadaDePrecio("c-1", "p-2", "Ratón", "https://img.test/p.png", new BigDecimal("20.00"), new BigDecimal("15.50"), HOY)));

        Document guardado = mongoTemplate.getCollection("alertas").find().first();

        assertNotNull(guardado);
        assertEquals("c-1", guardado.getString("clienteId"));
        assertEquals("p-2", guardado.getString("productoId"));
        assertEquals("https://img.test/p.png", guardado.getString("imagenProducto"));
        assertEquals("BAJADA_PRECIO", guardado.getString("tipo"));
        assertEquals(false, guardado.getBoolean("leida"));
        assertNotNull(guardado.getDate("fecha"));
        assertNotNull(guardado.getDate("createdAt"));
        assertTrue(guardado.get("precioNuevo") instanceof org.bson.types.Decimal128);
    }

    // Lectura de alertas (AB#531). PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU531-CP04, propuesto.
    @Test
    @DisplayName("HU531-CP04: una alerta se encuentra por su id solo con su cliente; con otro cliente o un id que no existe, no")
    void buscaUnaAlertaPorIdYCliente() {
        adapter.guardarTodas(List.of(Alerta.disponibilidad("c-1", "p-1", "Teclado", "https://img.test/p.png", HOY)));
        String id = adapter.buscarPorCliente("c-1").get(0).getId();

        assertTrue(adapter.buscarPorIdYCliente(id, "c-1").isPresent());
        assertTrue(adapter.buscarPorIdYCliente(id, "c-2").isEmpty());
        assertTrue(adapter.buscarPorIdYCliente("000000000000000000000000", "c-1").isEmpty());
    }

    @Test
    @DisplayName("HU531-CP04: guardar una alerta leída la actualiza sin duplicarla y conserva su fecha de creación")
    void guardarUnaAlertaLeidaLaActualiza() {
        adapter.guardarTodas(List.of(Alerta.disponibilidad("c-1", "p-1", "Teclado", "https://img.test/p.png", HOY)));
        Alerta nueva = adapter.buscarPorCliente("c-1").get(0);
        Object creadaEl = mongoTemplate.getCollection("alertas").find().first().get("createdAt");

        Alerta guardada = adapter.guardar(nueva.marcarComoLeida());

        assertTrue(guardada.isLeida());
        assertEquals(nueva.getId(), guardada.getId());
        assertEquals(1, mongoTemplate.getCollection("alertas").countDocuments());
        Document documento = mongoTemplate.getCollection("alertas").find().first();
        assertNotNull(documento);
        assertEquals(true, documento.getBoolean("leida"));
        assertEquals(creadaEl, documento.get("createdAt"));
        assertEquals("Teclado", documento.getString("nombreProducto"));
    }
}
