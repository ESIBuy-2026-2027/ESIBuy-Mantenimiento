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

    // MongoTemplate devuelve el documento tal cual está en MongoDB, con los nombres de sus campos.
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("tokens_seguridad").find().first();
        assertNotNull(guardado);
        return guardado;
    }
}
