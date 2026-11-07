package es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mongodb.MongoWriteException;
import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.metodospago.domain.model.MetodoPago;
import es.uclm.esibuy.backend.metodospago.domain.model.TipoMetodoPago;
import es.uclm.esibuy.backend.metodospago.infrastructure.adapter.out.seguridad.AesGcmCifradoDatosPagoAdapter;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
import java.util.Base64;
import java.util.List;
import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.mongodb.test.autoconfigure.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;

// Test de integración del adaptador de MongoDB de métodos de pago (HU6). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class MetodoPagoMongoAdapterTest {

    private static final int VALIDACION = 121; // "Document failed validation"

    @Autowired
    private MetodoPagoMongoRepository metodoPagoMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MetodoPagoMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        metodoPagoMongoRepository.deleteAll();
        adapter = new MetodoPagoMongoAdapter(metodoPagoMongoRepository, new MetodoPagoDocumentMapper());
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB, sin pasar por el mapper
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection("metodos_pago").find().first();
        assertNotNull(guardado);
        return guardado;
    }

    // El bloque cifrado es opaco para el adaptador: cualquier texto no vacío sirve
    private static MetodoPago nuevoMetodo(String clienteId) {
        return MetodoPago.nuevo(clienteId, TipoMetodoPago.TARJETA, "•••• 1111", "bloque-cifrado");
    }

    @Test
    @DisplayName("HU6-CP18: guardar un método nuevo le asigna un id")
    void guardarMetodoNuevoLeAsignaId() {
        MetodoPago guardado = adapter.guardar(nuevoMetodo("cliente-1"));

        assertNotNull(guardado.getId());
        assertEquals(1, metodoPagoMongoRepository.count());
    }

    @Test
    @DisplayName("HU6-CP18: buscar por cliente devuelve solo los métodos de ese cliente")
    void buscarPorClienteDevuelveSoloLosSuyos() {
        String propio = adapter.guardar(nuevoMetodo("cliente-1")).getId();
        adapter.guardar(nuevoMetodo("cliente-2"));

        List<MetodoPago> metodos = adapter.buscarPorCliente("cliente-1");

        assertEquals(List.of(propio), metodos.stream().map(MetodoPago::getId).toList());
    }

    @Test
    @DisplayName("HU6-CP18: actualizar un método conserva su fecha de creación")
    void actualizarConservaLaFechaDeCreacion() {
        MetodoPago guardado = adapter.guardar(nuevoMetodo("cliente-1"));
        Object creadoEn = documentoEnBruto().get("createdAt");
        assertNotNull(creadoEn);

        // Como hace el servicio al marcar el predeterminado: se guarda otra vez el mismo método
        guardado.marcarPredeterminado();
        adapter.guardar(guardado);

        Document actualizado = documentoEnBruto();
        assertEquals(creadoEn, actualizado.get("createdAt"));
        assertEquals(true, actualizado.get("predeterminado"));
    }

    @Test
    @DisplayName("HU6-CP18: eliminar un método lo borra de la colección")
    void eliminarBorraElMetodo() {
        String eliminado = adapter.guardar(nuevoMetodo("cliente-1")).getId();
        String conservado = adapter.guardar(nuevoMetodo("cliente-1")).getId();

        adapter.eliminar(eliminado);

        assertEquals(List.of(conservado),
                adapter.buscarPorCliente("cliente-1").stream().map(MetodoPago::getId).toList());
    }

    @Test
    @DisplayName("HU6-CP39: eliminar por cliente borra sus métodos de la colección y no los de otro cliente")
    void eliminarPorClienteBorraSoloSusMetodos() {
        adapter.guardar(nuevoMetodo("cliente-1"));
        adapter.guardar(nuevoMetodo("cliente-1"));
        String ajeno = adapter.guardar(nuevoMetodo("cliente-2")).getId();

        adapter.eliminarPorCliente("cliente-1");

        assertEquals(List.of(), adapter.buscarPorCliente("cliente-1"));
        assertEquals(List.of(ajeno),
                adapter.buscarPorCliente("cliente-2").stream().map(MetodoPago::getId).toList());
    }

    @Test
    @DisplayName("HU6-CP18: en MongoDB no queda ningún dato de la tarjeta en claro")
    void enMongoNoQuedaNingunDatoEnClaro() {
        // Criterio de aceptación de la HU6: se cifra con el adaptador real, como en la aplicación
        String clave = Base64.getEncoder().encodeToString(new byte[32]);
        String datosCifrados = new AesGcmCifradoDatosPagoAdapter(clave).cifrar("4111111111111111;Juan Pérez;12/27");
        adapter.guardar(MetodoPago.nuevo("cliente-1", TipoMetodoPago.TARJETA, "•••• 1111", datosCifrados));

        // Se revisa el documento entero tal cual está en MongoDB
        String enMongo = documentoEnBruto().toJson();
        assertFalse(enMongo.contains("4111111111111111"), enMongo);
        assertFalse(enMongo.contains("Juan Pérez"), enMongo);
        assertFalse(enMongo.contains("12/27"), enMongo);
    }

    @Test
    @DisplayName("HU6-CP18: la colección rechaza un documento sin datos cifrados")
    void laColeccionRechazaDocumentoSinDatosCifrados() {
        // Segunda barrera, a nivel de BBDD: aunque fallen las capas superiores, MongoDB no acepta un método
        // de pago sin el bloque cifrado
        Document sinDatosCifrados = new Document("clienteId", "cliente-1")
                .append("tipo", "TARJETA")
                .append("mascara", "•••• 1111")
                .append("predeterminado", false);

        MongoWriteException error = assertThrows(MongoWriteException.class,
                () -> mongoTemplate.getCollection("metodos_pago").insertOne(sinDatosCifrados));
        assertEquals(VALIDACION, error.getCode());
    }
}
