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

// Test de integración del adaptador de MongoDB de los códigos de verificación del login (HU2, T232).
// Usa un MongoDB real en Docker (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones
// crea tokens_seguridad con su validación y sus índices, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class CodigoVerificacionMongoAdapterTest {

    private static final Instant AHORA = Instant.parse("2026-10-09T10:00:00Z");

    @Autowired
    private CodigoVerificacionMongoRepository codigoVerificacionMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private CodigoVerificacionMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        codigoVerificacionMongoRepository.deleteAll();
        adapter = new CodigoVerificacionMongoAdapter(codigoVerificacionMongoRepository,
                new CodigoVerificacionDocumentMapper());
    }

    @Test
    @DisplayName("HU2-CP12: guardar el código lo deja en tokens_seguridad con su hash, nunca en claro")
    void guardaElCodigoEnTokensSeguridad() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));

        Document guardado = documentoEnBruto();
        assertEquals("u-1", guardado.getString("usuarioId"));
        assertEquals("hash-del-codigo", guardado.getString("tokenHash"));
        assertEquals("LOGIN_2FA", guardado.getString("purpose"));
        assertFalse(guardado.containsKey("codigo"));
        assertFalse(guardado.containsKey("_class"));
    }

    @Test
    @DisplayName("HU2-CP08: buscarPorUsuario devuelve el código guardado con su caducidad")
    void buscarPorUsuarioDevuelveElCodigoGuardado() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));

        CodigoVerificacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();

        assertNotNull(leido.getId());
        assertEquals("u-1", leido.getUsuarioId());
        assertEquals("hash-del-codigo", leido.getCodigoHash());
        assertEquals(AHORA.plus(Duration.ofMinutes(10)), leido.getExpiraEn());
        assertTrue(leido.puedeUsarse(AHORA));
        assertFalse(leido.puedeUsarse(AHORA.plus(Duration.ofMinutes(10))));
    }

    @Test
    @DisplayName("HU2-CP07: buscarPorUsuario devuelve vacío si el usuario no tiene código")
    void buscarPorUsuarioDevuelveVacioSinCodigo() {
        assertTrue(adapter.buscarPorUsuario("u-sin-codigo").isEmpty());
    }

    @Test
    @DisplayName("HU2-CP11: un login nuevo sustituye al código anterior del usuario")
    void unCodigoNuevoSustituyeAlAnterior() {
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-primer-login", AHORA));

        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-segundo-login", AHORA.plusSeconds(60)));

        assertEquals(1, codigoVerificacionMongoRepository.count());
        assertEquals("hash-segundo-login", adapter.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
    }

    @Test
    @DisplayName("HU2-CP09: guardar un código leído actualiza el mismo documento con su uso y sus intentos")
    void guardarUnCodigoLeidoLoActualiza() {
        // Es lo que hace el paso 2 del login: leer el código, registrar un fallo o marcarlo como usado, y guardar.
        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));
        Date creado = documentoEnBruto().getDate("createdAt");
        CodigoVerificacion leido = adapter.buscarPorUsuario("u-1").orElseThrow();
        leido.registrarIntentoFallido();
        leido.marcarUsado(AHORA.plusSeconds(30));

        adapter.guardar(leido);

        Document actualizado = documentoEnBruto();
        assertEquals(1, codigoVerificacionMongoRepository.count());
        assertEquals(1, actualizado.getInteger("attempts"));
        assertEquals(Date.from(AHORA.plusSeconds(30)), actualizado.getDate("usedAt"));
        assertNotNull(creado);
        assertEquals(creado, actualizado.getDate("createdAt"));
    }

    @Test
    @DisplayName("HU2-CP07: los tokens de otro propósito del mismo usuario no se devuelven ni se borran")
    void noDevuelveTokensDeOtroProposito() {
        // tokens_seguridad guarda también, p. ej., los tokens de recuperación de contraseña (P7 del plan de la T232).
        mongoTemplate.getCollection("tokens_seguridad").insertOne(new Document("usuarioId", "u-1")
                .append("purpose", "PASSWORD_RECOVERY").append("tokenHash", "hash-de-recuperacion")
                .append("expiresAt", Date.from(AHORA.plus(Duration.ofHours(1)))));
        assertTrue(adapter.buscarPorUsuario("u-1").isEmpty());

        adapter.guardar(CodigoVerificacion.nuevo("u-1", "hash-del-codigo", AHORA));

        assertEquals("hash-del-codigo", adapter.buscarPorUsuario("u-1").orElseThrow().getCodigoHash());
        assertEquals(2, codigoVerificacionMongoRepository.count());
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB, con los nombres de sus campos.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("tokens_seguridad").find().first();
        assertNotNull(guardado);
        return guardado;
    }
}
