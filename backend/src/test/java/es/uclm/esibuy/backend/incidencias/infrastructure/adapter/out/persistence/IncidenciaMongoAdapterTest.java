package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Date;
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
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;
import es.uclm.esibuy.backend.shared.config.MongoConfig;
import es.uclm.esibuy.backend.shared.infrastructure.config.InicializadorColecciones;

// Test de integración del adaptador de MongoDB de las incidencias (HU44, T291). Usa un MongoDB real en Docker
// (Testcontainers): Docker Desktop debe estar abierto. InicializadorColecciones crea las colecciones con su
// validación y sus índices, como en la aplicación real.
// También la búsqueda de todas las incidencias para el administrador (HU49, T307).
// Y la búsqueda por id para abrir una incidencia (HU50, T302).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU44-CP07, HU49-CP04 y HU50-CP08, propuestos.
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

    
    private static Incidencia incidencia(String autorId, String asunto, int segundos) {
        return Incidencia.nueva(autorId, asunto, "Descripción", AHORA.plusSeconds(segundos));
    }

    private static List<String> asuntos(IncidenciaPage pagina) {
        return pagina.incidencias().stream().map(Incidencia::asunto).toList();
    }

    @Test
    @DisplayName("HU44-CP16: buscar por autor devuelve solo sus incidencias, de las más antiguas a las más recientes")
    void buscarPorAutorDevuelveSoloLasSuyasDeLasMasAntiguasALasMasRecientes() {
        adapter.guardar(incidencia("u-1", "Tercera", 30));
        adapter.guardar(incidencia("u-2", "De otro", 5));
        adapter.guardar(incidencia("u-1", "Primera", 10));
        adapter.guardar(incidencia("u-1", "Segunda", 20));

        IncidenciaPage pagina = adapter.buscarPorAutor("u-1", 0, 10);

        assertEquals(List.of("Primera", "Segunda", "Tercera"), asuntos(pagina));
        assertEquals(3, pagina.totalElementos());
    }

    @Test
    @DisplayName("HU44-CP16: devuelve una página cada vez, con el total de todas")
    void devuelveUnaPaginaCadaVezConElTotalDeTodas() {
        for (int i = 1; i <= 5; i++) {
            adapter.guardar(incidencia("u-1", "Incidencia " + i, i));
        }

        IncidenciaPage primera = adapter.buscarPorAutor("u-1", 0, 2);
        IncidenciaPage ultima = adapter.buscarPorAutor("u-1", 2, 2);

        assertEquals(List.of("Incidencia 1", "Incidencia 2"), asuntos(primera));
        assertEquals(0, primera.pagina());
        assertEquals(2, primera.tamano());
        assertEquals(5, primera.totalElementos());
        assertEquals(List.of("Incidencia 5"), asuntos(ultima));
        assertEquals(2, ultima.pagina());
        assertEquals(5, ultima.totalElementos());
    }

    @Test
    @DisplayName("HU44-CP16: con la misma fecha conserva el orden en que se guardaron, para que las páginas sean estables")
    void conLaMismaFechaConservaElOrdenDeGuardado() {
        adapter.guardar(incidencia("u-1", "Una", 0));
        adapter.guardar(incidencia("u-1", "Dos", 0));
        adapter.guardar(incidencia("u-1", "Tres", 0));

        assertEquals(List.of("Una", "Dos", "Tres"), asuntos(adapter.buscarPorAutor("u-1", 0, 10)));
    }

    @Test
    @DisplayName("HU44-CP16: una página más allá de la última viene vacía, pero con el total")
    void unaPaginaMasAllaDeLaUltimaVieneVaciaConElTotal() {
        adapter.guardar(incidencia("u-1", "Una", 0));

        IncidenciaPage pagina = adapter.buscarPorAutor("u-1", 3, 10);

        assertEquals(List.of(), asuntos(pagina));
        assertEquals(1, pagina.totalElementos());
    }

    @Test
    @DisplayName("HU44-CP16: un autor sin incidencias recibe una página vacía")
    void unAutorSinIncidenciasRecibeUnaPaginaVacia() {
        adapter.guardar(incidencia("u-2", "De otro", 0));

        IncidenciaPage pagina = adapter.buscarPorAutor("u-1", 0, 10);

        assertEquals(List.of(), asuntos(pagina));
        assertEquals(0, pagina.totalElementos());
    }

    // Supervisión de incidencias (HU49, T307): el administrador ve las de todos los autores, en el mismo orden

    @Test
    @DisplayName("HU49-CP04: buscar todas devuelve las de todos los autores, de las más antiguas a las más recientes")
    void buscarTodasDevuelveLasDeTodosLosAutoresDeLasMasAntiguasALasMasRecientes() {
        adapter.guardar(incidencia("u-1", "Tercera", 30));
        adapter.guardar(incidencia("u-2", "Primera", 10));
        adapter.guardar(incidencia("u-1", "Segunda", 20));
        adapter.guardar(incidencia("u-2", "Misma fecha que la tercera", 30));

        IncidenciaPage pagina = adapter.buscarTodas(0, 10);

        assertEquals(List.of("Primera", "Segunda", "Tercera", "Misma fecha que la tercera"), asuntos(pagina));
        assertEquals(List.of("u-2", "u-1", "u-1", "u-2"), pagina.incidencias().stream().map(Incidencia::autorId).toList());
        assertEquals(4, pagina.totalElementos());
    }

    @Test
    @DisplayName("HU49-CP04: buscar todas devuelve una página cada vez, con el total de todas")
    void buscarTodasDevuelveUnaPaginaCadaVezConElTotalDeTodas() {
        for (int i = 1; i <= 5; i++) {
            adapter.guardar(incidencia(i % 2 == 0 ? "u-2" : "u-1", "Incidencia " + i, i));
        }

        IncidenciaPage segunda = adapter.buscarTodas(1, 2);

        assertEquals(List.of("Incidencia 3", "Incidencia 4"), asuntos(segunda));
        assertEquals(1, segunda.pagina());
        assertEquals(2, segunda.tamano());
        assertEquals(5, segunda.totalElementos());
    }

    @Test
    @DisplayName("HU49-CP04: sin incidencias, buscar todas devuelve una página vacía")
    void sinIncidenciasBuscarTodasDevuelveUnaPaginaVacia() {
        IncidenciaPage pagina = adapter.buscarTodas(0, 10);

        assertEquals(List.of(), asuntos(pagina));
        assertEquals(0, pagina.totalElementos());
    }

    // Resolución de incidencias (HU50): el administrador abre una incidencia concreta por su id

    @Test
    @DisplayName("HU50-CP08: buscar por id devuelve la incidencia guardada con todos sus datos")
    void buscarPorIdDevuelveLaIncidenciaGuardada() {
        Incidencia guardada = adapter.guardar(incidencia("u-1", "No me llega el pedido", 0));
        adapter.guardar(incidencia("u-2", "Otra", 5));

        Incidencia encontrada = adapter.buscarPorId(guardada.id()).orElseThrow();

        assertEquals(guardada, encontrada);
    }

    @Test
    @DisplayName("HU50-CP08: buscar por id una incidencia que no existe devuelve vacío")
    void buscarPorIdUnaQueNoExisteDevuelveVacio() {
        adapter.guardar(incidencia("u-1", "Una", 0));

        assertTrue(adapter.buscarPorId("no-existe").isEmpty());
    }

    @Test
    @DisplayName("HU50-CP08: guardar una incidencia abierta actualiza su estado y su respuesta, sin crear otra")
    void guardarUnaIncidenciaAbiertaLaActualiza() {
        Incidencia pendiente = adapter.guardar(incidencia("u-1", "No me llega el pedido", 0));

        adapter.guardar(pendiente.abrir("Lo estamos revisando"));

        Incidencia encontrada = adapter.buscarPorId(pendiente.id()).orElseThrow();
        assertEquals(EstadoIncidencia.ABIERTA, encontrada.estado());
        assertEquals("Lo estamos revisando", encontrada.respuesta());
        assertEquals(1, incidenciaMongoRepository.count());
    }
}