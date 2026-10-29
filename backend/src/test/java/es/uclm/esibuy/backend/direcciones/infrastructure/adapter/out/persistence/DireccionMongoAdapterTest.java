package es.uclm.esibuy.backend.direcciones.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.mongodb.MongoWriteException;
import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.direcciones.domain.model.CodigoPostal;
import es.uclm.esibuy.backend.direcciones.domain.model.DatosDireccion;
import es.uclm.esibuy.backend.direcciones.domain.model.DireccionEnvio;
import es.uclm.esibuy.backend.direcciones.domain.model.TelefonoContacto;
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

// Test de integración del adaptador de MongoDB de direcciones de envío (HU5). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación, como en la aplicación real.
@DataMongoTest
@Import({MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class})
class DireccionMongoAdapterTest {

    private static final int VALIDACION = 121; // "Document failed validation"
    private static final String COLECCION = "direcciones_envio";

    @Autowired
    private DireccionMongoRepository direccionMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private DireccionMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        direccionMongoRepository.deleteAll();
        adapter = new DireccionMongoAdapter(direccionMongoRepository, new DireccionDocumentMapper());
    }

    // MongoTemplate devuelve el documento tal cual está en MongoDB, sin pasar por el mapper
    private Document documentoEnBruto() {
        Document guardado = mongoTemplate.getCollection(COLECCION).find().first();
        assertNotNull(guardado);
        return guardado;
    }

    private static DatosDireccion datos(String calle) {
        return new DatosDireccion("Juan Pérez", calle, "2º B", new CodigoPostal("13071"), "Ciudad Real",
                "Ciudad Real", new TelefonoContacto("612345678"));
    }

    private static DireccionEnvio nuevaDireccion(String clienteId) {
        return DireccionEnvio.nueva(clienteId, datos("Calle Mayor 1"));
    }

    // Un documento con todo lo que exige la colección, para quitarle un campo en cada prueba
    private static Document documentoCompleto() {
        return new Document("clienteId", "cliente-1")
                .append("destinatario", "Juan Pérez")
                .append("calle", "Calle Mayor 1")
                .append("codigoPostal", "13071")
                .append("ciudad", "Ciudad Real")
                .append("provincia", "Ciudad Real")
                .append("telefono", "612345678")
                .append("principal", false);
    }

    @Test
    @DisplayName("HU5-CP10: guardar una dirección nueva le asigna un id y guarda todos sus datos")
    void guardarDireccionNuevaLeAsignaId() {
        DireccionEnvio guardada = adapter.guardar(nuevaDireccion("cliente-1"));

        assertNotNull(guardada.getId());
        assertEquals(datos("Calle Mayor 1"), guardada.getDatos());
        assertEquals(1, direccionMongoRepository.count());
    }

    @Test
    @DisplayName("HU5-CP10: buscar por cliente devuelve solo las direcciones de ese cliente")
    void buscarPorClienteDevuelveSoloLasSuyas() {
        String propia = adapter.guardar(nuevaDireccion("cliente-1")).getId();
        adapter.guardar(nuevaDireccion("cliente-2"));

        List<DireccionEnvio> direcciones = adapter.buscarPorCliente("cliente-1");

        assertEquals(List.of(propia), direcciones.stream().map(DireccionEnvio::getId).toList());
    }

    @Test
    @DisplayName("HU5-CP10: actualizar una dirección guarda los cambios y conserva su fecha de creación")
    void actualizarConservaLaFechaDeCreacion() {
        DireccionEnvio guardada = adapter.guardar(nuevaDireccion("cliente-1"));
        Object creadaEn = documentoEnBruto().get("createdAt");
        assertNotNull(creadaEn);

        // Como hace el servicio al editar y al marcar la principal: se guarda otra vez la misma dirección
        guardada.editar(datos("Calle Toledo 5"));
        guardada.marcarPrincipal();
        adapter.guardar(guardada);

        Document actualizada = documentoEnBruto();
        assertEquals(creadaEn, actualizada.get("createdAt"));
        assertEquals("Calle Toledo 5", actualizada.get("calle"));
        assertEquals(true, actualizada.get("principal"));
        assertEquals(1, direccionMongoRepository.count());
    }

    @Test
    @DisplayName("HU5-CP10: eliminar una dirección la borra de la colección")
    void eliminarBorraLaDireccion() {
        String eliminada = adapter.guardar(nuevaDireccion("cliente-1")).getId();
        String conservada = adapter.guardar(nuevaDireccion("cliente-1")).getId();

        adapter.eliminar(eliminada);

        assertEquals(List.of(conservada),
                adapter.buscarPorCliente("cliente-1").stream().map(DireccionEnvio::getId).toList());
    }

    @Test
    @DisplayName("HU5-CP10: la colección acepta una dirección completa y rechaza una sin un campo obligatorio")
    void laColeccionRechazaDocumentoIncompleto() {
        // Segunda barrera, a nivel de BBDD: aunque fallen las capas superiores, MongoDB no acepta una dirección
        // a la que le falte un dato para enviar el pedido
        mongoTemplate.getCollection(COLECCION).insertOne(documentoCompleto());

        for (String campo : List.of("clienteId", "destinatario", "calle", "codigoPostal", "ciudad", "provincia",
                "telefono", "principal")) {
            Document incompleto = documentoCompleto();
            incompleto.remove(campo);

            MongoWriteException error = assertThrows(MongoWriteException.class,
                    () -> mongoTemplate.getCollection(COLECCION).insertOne(incompleto), campo);
            assertEquals(VALIDACION, error.getCode(), campo);
        }
    }

    @Test
    @DisplayName("HU5-CP10: la colección rechaza un código postal o un teléfono con formato inválido")
    void laColeccionRechazaFormatosInvalidos() {
        Document codigoPostalInvalido = documentoCompleto().append("codigoPostal", "1307");
        Document telefonoInvalido = documentoCompleto().append("telefono", "61234567A");

        for (Document invalido : List.of(codigoPostalInvalido, telefonoInvalido)) {
            MongoWriteException error = assertThrows(MongoWriteException.class,
                    () -> mongoTemplate.getCollection(COLECCION).insertOne(invalido));
            assertEquals(VALIDACION, error.getCode());
        }
    }
}
