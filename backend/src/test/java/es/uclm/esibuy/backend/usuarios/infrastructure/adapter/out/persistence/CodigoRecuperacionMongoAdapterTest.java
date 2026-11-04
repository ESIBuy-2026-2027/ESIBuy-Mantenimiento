package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.time.Duration;
import java.time.Instant;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Test de integración del adaptador de MongoDB de los códigos de recuperación de contraseña (HU3, T400).
// Se guardan en tokens_seguridad, como los del login, pero con purpose PASSWORD_RECOVERY: unos y otros no se pisan.
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class CodigoRecuperacionMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-10-31T10:00:00Z");

    @Autowired
    private CodigoVerificacionMongoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private CodigoRecuperacionMongoAdapter adapter;

    // El adaptador del login, para comprobar que los dos tipos de código conviven sin pisarse.
    private CodigoVerificacionMongoAdapter adapterDelLogin;

    @BeforeEach
    void preparar() {
        repository.deleteAll();
        adapter = new CodigoRecuperacionMongoAdapter(repository, new CodigoVerificacionDocumentMapper());
        adapterDelLogin = new CodigoVerificacionMongoAdapter(repository, new CodigoVerificacionDocumentMapper());
    }

    @Test
    @DisplayName("HU3-CP10: guardar el código lo deja en tokens_seguridad como de recuperación, solo con su hash")
    void guardaElCodigoComoDeRecuperacion() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));

        Document guardado = mongoTemplate.getCollection("tokens_seguridad").find().first();
        assertNotNull(guardado);
        assertEquals("u-1", guardado.getString("usuarioId"));
        assertEquals("hash-del-codigo", guardado.getString("tokenHash"));
        assertEquals("PASSWORD_RECOVERY", guardado.getString("purpose"));
        assertFalse(guardado.containsKey("codigo"));
    }

    @Test
    @DisplayName("HU3-CP10: buscarPorUsuario devuelve el código de recuperación con su caducidad de 10 minutos")
    void buscarPorUsuarioDevuelveElCodigoGuardado() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));

        CodigoVerificacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();

        assertNotNull(leido.getId());
        assertEquals("hash-del-codigo", leido.getCodigoHash());
        assertEquals(AHORA.plus(Duration.ofMinutes(10)), leido.getExpiraEn());
        assertTrue(adapter.buscarPorUsuario("u-sin-codigo").isEmpty());
    }

    @Test
    @DisplayName("HU3-CP10: pedir otro código sustituye al de recuperación anterior, que deja de valer")
    void unCodigoNuevoSustituyeAlAnterior() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-primero", AHORA));

        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-segundo", AHORA.plusSeconds(60)));

        assertEquals(1, repository.count());
        assertEquals("hash-segundo", adapter.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
    }

    @Test
    @DisplayName("HU3-CP10: guardar un código leído actualiza el mismo documento con su uso y sus intentos")
    void guardarUnCodigoLeidoLoActualiza() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));
        Date creado = mongoTemplate.getCollection("tokens_seguridad").find().first().getDate("createdAt");
        CodigoVerificacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();
        leido.registrarIntentoFallido();
        leido.marcarUsado(AHORA.plusSeconds(30));

        adapter.guardar(leido);

        Document actualizado = mongoTemplate.getCollection("tokens_seguridad").find().first();
        assertEquals(1, repository.count());
        assertEquals("PASSWORD_RECOVERY", actualizado.getString("purpose"));
        assertEquals(1, actualizado.getInteger("attempts"));
        assertEquals(Date.from(AHORA.plusSeconds(30)), actualizado.getDate("usedAt"));
        assertEquals(creado, actualizado.getDate("createdAt"));
    }

    @Test
    @DisplayName("HU3-CP10: el código de recuperación y el del login del mismo usuario no se pisan ni se confunden")
    void elCodigoDeRecuperacionYElDelLoginNoSePisan() {
        adapterDelLogin.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-login", AHORA));
        assertTrue(adapter.buscarPorUsuario("u-1").isEmpty());

        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-de-recuperacion", AHORA));
        adapterDelLogin.guardar(CodigoVerificacion.nuevo("u-1", "hash-de-otro-login", AHORA));

        assertEquals(2, repository.count());
        assertEquals("hash-de-recuperacion", adapter.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
        assertEquals("hash-de-otro-login", adapterDelLogin.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
    }

    @Test
    @DisplayName("HU47-CP02: eliminar por usuario borra el código de recuperación y conserva el del login")
    void eliminarPorUsuarioBorraSoloElCodigoDeRecuperacion() {
        adapterDelLogin.guardar(CodigoVerificacion.nuevo("u-1", "hash-login", AHORA));
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-recuperacion", AHORA));

        adapter.eliminarPorUsuario("u-1");

        assertTrue(adapter.buscarPorUsuario("u-1").isEmpty());
        assertEquals("hash-login", adapterDelLogin.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
    }
}
