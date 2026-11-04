package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB del factor de Authenticator (HU2, T20). Usa un MongoDB real
// en Docker (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea
// factores_autenticacion con su validación y su índice único por usuario, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class FactorAutenticacionMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-10-11T10:00:00Z");
    private static final String COLECCION = "factores_autenticacion";

    @Autowired
    private FactorAutenticacionMongoRepository factorAutenticacionMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private FactorAutenticacionMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        factorAutenticacionMongoRepository.deleteAll();
        adapter = new FactorAutenticacionMongoAdapter(factorAutenticacionMongoRepository,
                new FactorAutenticacionDocumentMapper());
    }

    @Test
    @DisplayName("HU2-CP24: guardar el factor lo deja en factores_autenticacion con el secreto cifrado")
    void guardaElFactorEnFactoresAutenticacion() {
        adapter.guardar(FactorAutenticacion.nuevo("u-1", "secreto-cifrado"));

        Document guardado = mongoTemplate.getCollection(COLECCION).find().first();
        assertEquals("u-1", guardado.getString("usuarioId"));
        assertEquals("secreto-cifrado", guardado.getString("totpSecretEncrypted"));
        assertFalse(guardado.getBoolean("totpEnabled"));
        assertFalse(guardado.containsKey("_class"));
    }

    @Test
    @DisplayName("HU2-CP23: buscar por usuario devuelve el factor guardado con su alta confirmada")
    void buscarPorUsuarioDevuelveElFactorGuardado() {
        FactorAutenticacion factor = FactorAutenticacion.nuevo("u-1", "secreto-cifrado");
        factor.registrarUso(5, AHORA);
        adapter.guardar(factor);

        FactorAutenticacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();

        assertEquals("secreto-cifrado", leido.getSecretoCifrado());
        assertTrue(leido.estaConfirmado());
        assertEquals(AHORA, leido.getVerificadoEn());
        assertFalse(leido.puedeUsarPeriodo(5));
    }

    @Test
    @DisplayName("HU2-CP22: un usuario sin Authenticator no tiene factor")
    void buscarPorUsuarioDevuelveVacioSinFactor() {
        assertTrue(adapter.buscarPorUsuario("u-1").isEmpty());
    }

    @Test
    @DisplayName("HU2-CP20: guardar un factor leído lo actualiza sin duplicarlo")
    void guardarUnFactorLeidoLoActualiza() {
        adapter.guardar(FactorAutenticacion.nuevo("u-1", "secreto-cifrado"));
        FactorAutenticacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();

        leido.registrarUso(5, AHORA);
        adapter.guardar(leido);

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        assertFalse(adapter.buscarPorUsuario("u-1").orElseThrow().puedeUsarPeriodo(5));
    }

    @Test
    @DisplayName("HU2-CP22: empezar un alta nueva sustituye al factor anterior del usuario")
    void unAltaNuevaSustituyeAlFactorAnterior() {
        // Si el usuario recarga la pantalla del QR, se genera otro secreto: el anterior ya no debe valer.
        // El índice único de usuarioId impediría guardar los dos.
        adapter.guardar(FactorAutenticacion.nuevo("u-1", "secreto-viejo"));

        adapter.guardar(FactorAutenticacion.nuevo("u-1", "secreto-nuevo"));

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        assertEquals("secreto-nuevo", adapter.buscarPorUsuario("u-1").orElseThrow().getSecretoCifrado());
    }

    @Test
    @DisplayName("HU47-CP02: eliminar por usuario borra el factor del vendedor")
    void eliminarPorUsuarioBorraElFactor() {
        adapter.guardar(FactorAutenticacion.nuevo("u-1", "secreto-uno"));
        adapter.guardar(FactorAutenticacion.nuevo("u-2", "secreto-dos"));

        adapter.eliminarPorUsuario("u-1");

        assertTrue(adapter.buscarPorUsuario("u-1").isEmpty());
        assertTrue(adapter.buscarPorUsuario("u-2").isPresent());
    }
}
