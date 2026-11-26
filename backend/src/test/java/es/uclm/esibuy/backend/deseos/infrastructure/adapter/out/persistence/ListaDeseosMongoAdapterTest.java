package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.persistence;

import java.util.Date;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.springframework.dao.DuplicateKeyException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.MongoTestcontainersConfig;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;
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

// Test de integración del adaptador de MongoDB de la lista de deseos (HU14). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
@DataMongoTest
@Import({ MongoTestcontainersConfig.class, MongoConfig.class, InicializadorColecciones.class })
class ListaDeseosMongoAdapterTest {

    private static final String COLECCION = "listas_deseos";
    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant HOY = Instant.parse("2026-11-04T10:00:00Z");

    @Autowired
    private ListaDeseosMongoRepository listaDeseosMongoRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    private ListaDeseosMongoAdapter adapter;

    @BeforeEach
    void preparar() {
        listaDeseosMongoRepository.deleteAll();
        adapter = new ListaDeseosMongoAdapter(listaDeseosMongoRepository, new ListaDeseosDocumentMapper());
    }

    private static ListaDeseos listaNueva(String clienteId) {
        return new ListaDeseos(null, clienteId, List.of(new ItemDeseo("p-1", AYER)));
    }

    @Test
    @DisplayName("HU14-CP06: guardar una lista nueva le asigna un id y se recupera por su cliente")
    void guardarUnaListaNuevaYRecuperarlaPorCliente() {
        ListaDeseos guardada = adapter.guardar(listaNueva("cliente-1"));

        assertNotNull(guardada.getId());
        ListaDeseos leida = adapter.buscarFavoritos("cliente-1").orElseThrow();
        assertEquals(guardada.getId(), leida.getId());
        assertEquals(List.of(new ItemDeseo("p-1", AYER)), leida.getItems());
        assertTrue(adapter.buscarFavoritos("cliente-2").isEmpty());
    }

    @Test
    @DisplayName("HU14-CP06: guardar otra vez la lista la actualiza sin crear otra y conserva su fecha de creación")
    void guardarOtraVezActualizaLaLista() {
        ListaDeseos guardada = adapter.guardar(listaNueva("cliente-1"));
        Object creadaEn = mongoTemplate.getCollection(COLECCION).find().first().get("createdAt");
        assertNotNull(creadaEn);

        guardada.anadir("p-2", HOY);
        adapter.guardar(guardada);

        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
        Document documento = mongoTemplate.getCollection(COLECCION).find().first();
        assertEquals(creadaEn, documento.get("createdAt"));
        assertEquals(2, adapter.buscarFavoritos("cliente-1").orElseThrow().getItems().size());
    }

    @Test
    @DisplayName("HU14-CP43: eliminar por cliente borra su lista de la colección y no la de otro cliente")
    void eliminarPorClienteBorraSoloSuLista() {
        adapter.guardar(listaNueva("cliente-1"));
        adapter.guardar(listaNueva("cliente-2"));

        adapter.eliminarPorCliente("cliente-1");

        assertTrue(adapter.buscarFavoritos("cliente-1").isEmpty());
        assertTrue(adapter.buscarFavoritos("cliente-2").isPresent());
    }

    @Test
    @DisplayName("PBI534-CP03: un cliente puede guardar varias listas con nombre y eliminar una sin afectar a las demás")
    void unClientePuedeTenerVariasListasConNombre() {
        ListaDeseos primera = adapter.guardar(ListaDeseos.nueva("cliente-1", "Regalos"));
        ListaDeseos segunda = adapter.guardar(ListaDeseos.nueva("cliente-1", "Oficina"));

        List<ListaDeseos> todas = adapter.buscarListas("cliente-1");
        assertEquals(2, todas.size());
        assertEquals(List.of("Regalos", "Oficina"), todas.stream().map(ListaDeseos::getNombre).toList());

        adapter.eliminar(primera.getId(), "cliente-1");
        assertEquals(List.of(segunda.getId()),
                adapter.buscarListas("cliente-1").stream().map(ListaDeseos::getId).toList());
    }

    @Test
    @DisplayName("HU528-CP33: los favoritos y las listas del cliente se guardan y se leen por separado")
    void losFavoritosYLasListasSeLeenPorSeparado() {
        ListaDeseos favoritos = adapter.guardar(listaNueva("cliente-1"));
        ListaDeseos lista = adapter.guardar(ListaDeseos.nueva("cliente-1", "Regalos"));

        assertEquals(favoritos.getId(), adapter.buscarFavoritos("cliente-1").orElseThrow().getId());
        assertEquals(List.of(lista.getId()),
                adapter.buscarListas("cliente-1").stream().map(ListaDeseos::getId).toList());
        assertTrue(adapter.buscarLista(lista.getId(), "cliente-1").isPresent());
        assertTrue(adapter.buscarLista(lista.getId(), "cliente-2").isEmpty());
        assertTrue(adapter.buscarLista(favoritos.getId(), "cliente-1").isEmpty());
    }

    @Test
    @DisplayName("HU528-CP33: la colección solo admite unos favoritos por cliente")
    void soloUnosFavoritosPorCliente() {
        adapter.guardar(listaNueva("cliente-1"));
        ListaDeseos repetidos = listaNueva("cliente-1");

        assertThrows(DuplicateKeyException.class, () -> adapter.guardar(repetidos));
    }

    // Lo que hay guardado de antes de separarlos: sin el campo tipo
    private void guardarAntigua(String nombre, String productoId, Instant creadaEn) {
        Document antigua = new Document("usuarioId", "cliente-1").append("createdAt", Date.from(creadaEn))
                .append("items", List.of(new Document("productoId", productoId).append("addedAt", Date.from(AYER))));
        if (nombre != null) {
            antigua.append("nombre", nombre);
        }
        mongoTemplate.getCollection(COLECCION).insertOne(antigua);
    }

    @Test
    @DisplayName("HU528-CP34: la lista única que había antes, sin nombre, pasa a ser los favoritos y al guardarla queda marcada")
    void laListaAntiguaSinNombreSonLosFavoritos() {
        guardarAntigua(null, "p-1", AYER);

        ListaDeseos favoritos = adapter.buscarFavoritos("cliente-1").orElseThrow();
        assertEquals(List.of(new ItemDeseo("p-1", AYER)), favoritos.getItems());
        assertTrue(adapter.buscarListas("cliente-1").isEmpty());

        adapter.guardar(favoritos);

        assertEquals("FAVORITOS", mongoTemplate.getCollection(COLECCION).find().first().getString("tipo"));
        assertEquals(1, mongoTemplate.getCollection(COLECCION).countDocuments());
    }

    @Test
    @DisplayName("HU528-CP34: de las listas antiguas, la primera llamada Favoritos son los favoritos y las demás siguen siendo listas")
    void deLasListasAntiguasSoloLaPrimeraFavoritosLoEs() {
        guardarAntigua("Favoritos", "p-1", AYER);
        guardarAntigua("Regalos", "p-2", HOY);
        guardarAntigua("Favoritos", "p-3", HOY.plusSeconds(60));

        assertEquals(List.of(new ItemDeseo("p-1", AYER)),
                adapter.buscarFavoritos("cliente-1").orElseThrow().getItems());
        assertEquals(List.of("Regalos", "Favoritos"),
                adapter.buscarListas("cliente-1").stream().map(ListaDeseos::getNombre).toList());
    }

    @Test
    @DisplayName("HU528-CP34: una lista antigua a la que el cliente cambió el nombre sigue siendo una lista")
    void unaListaAntiguaRenombradaSigueSiendoLista() {
        guardarAntigua("Cumpleaños", "p-1", AYER);

        assertTrue(adapter.buscarFavoritos("cliente-1").isEmpty());
        assertEquals(List.of("Cumpleaños"),
                adapter.buscarListas("cliente-1").stream().map(ListaDeseos::getNombre).toList());
    }

    @Test
    @DisplayName("HU529-CP12: devuelve, sin repetir, los clientes que tienen el producto en sus favoritos, pero no en sus listas de deseos")
    void devuelveLosClientesConElProductoEnFavoritos() {
        adapter.guardar(new ListaDeseos(null, "cliente-1", List.of(new ItemDeseo("p-1", AYER))));
        adapter.guardar(new ListaDeseos(null, "cliente-2", List.of(new ItemDeseo("p-2", AYER),
                new ItemDeseo("p-1", HOY))));
        adapter.guardar(new ListaDeseos(null, "cliente-3", List.of(new ItemDeseo("p-2", AYER))));
        ListaDeseos lista = ListaDeseos.nueva("cliente-4", "Cumpleaños");
        lista.anadir("p-1", HOY);
        adapter.guardar(lista);

        assertEquals(List.of("cliente-1", "cliente-2"), adapter.clientesConFavorito("p-1").stream().sorted().toList());
        assertTrue(adapter.clientesConFavorito("p-9").isEmpty());
    }

    @Test
    @DisplayName("HU528-CP33: crear la lista por defecto dos veces a la vez no la duplica ni pierde sus productos")
    void laListaPorDefectoNoSeDuplica() {
        ListaDeseos primera = adapter.crearListaPorDefecto("cliente-1");
        primera.anadir("p-1", HOY);
        adapter.guardar(primera);

        ListaDeseos segunda = adapter.crearListaPorDefecto("cliente-1");

        assertEquals(primera.getId(), segunda.getId());
        assertEquals(List.of(new ItemDeseo("p-1", HOY)), segunda.getItems());
        assertEquals(1, adapter.buscarListas("cliente-1").size());
        assertEquals("Mi lista de deseos", segunda.getNombre());
        assertNotNull(mongoTemplate.getCollection(COLECCION).find().first().get("createdAt"));
        assertEquals(1, adapter.buscarListas("cliente-1").get(0).getItems().size());
    }
}
