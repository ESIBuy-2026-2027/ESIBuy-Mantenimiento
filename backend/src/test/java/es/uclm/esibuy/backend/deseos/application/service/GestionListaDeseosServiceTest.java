package es.uclm.esibuy.backend.deseos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba
class GestionListaDeseosServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final String CLIENTE = "c-1";
    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant AHORA = Instant.parse("2026-11-04T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    // p-1 y p-2 están disponibles; cualquier otro no existe o no está disponible
    private final GestionListaDeseosService servicio = new GestionListaDeseosService(repositorio,
            email -> Map.of(EMAIL, CLIENTE).get(email), Set.of("p-1", "p-2")::contains, RELOJ);

    @Test
    @DisplayName("HU14-CP01: el primer producto crea la lista del cliente en sesión y la guarda con la fecha")
    void elPrimerProductoCreaLaListaDelCliente() {
        ListaDeseos devuelta = servicio.anadir(EMAIL, "p-1");

        ListaDeseos guardada = repositorio.buscarPorCliente(CLIENTE).orElseThrow();
        assertEquals(List.of(new ItemDeseo("p-1", AHORA)), guardada.getItems());
        assertEquals("l-1", devuelta.getId());
    }

    @Test
    @DisplayName("HU14-CP02: añadir otro producto usa la lista que ya tiene el cliente y conserva los anteriores")
    void anadirOtroProductoUsaLaListaDelCliente() {
        repositorio.guardar(new ListaDeseos(null, CLIENTE, List.of(new ItemDeseo("p-1", AYER))));

        servicio.anadir(EMAIL, "p-2");

        assertEquals(1, repositorio.listas.size());
        assertEquals(List.of(new ItemDeseo("p-1", AYER), new ItemDeseo("p-2", AHORA)),
                repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems());
    }

    @Test
    @DisplayName("HU14-CP03: añadir un producto que ya está no es un error y la lista no cambia")
    void anadirUnProductoQueYaEstaNoEsUnError() {
        repositorio.guardar(new ListaDeseos(null, CLIENTE, List.of(new ItemDeseo("p-1", AYER))));

        ListaDeseos devuelta = servicio.anadir(EMAIL, "p-1");

        assertEquals(List.of(new ItemDeseo("p-1", AYER)), devuelta.getItems());
    }

    @Test
    @DisplayName("HU14-CP04: un producto inexistente o no disponible se rechaza y no se crea la lista")
    void unProductoNoDisponibleNoCreaLaLista() {
        assertThrows(ProductoNoDisponibleException.class, () -> servicio.anadir(EMAIL, "p-9"));

        assertTrue(repositorio.listas.isEmpty());
    }

    @Test
    @DisplayName("HU14-CP04: un producto inexistente o no disponible no cambia la lista que ya tiene el cliente")
    void unProductoNoDisponibleNoCambiaLaLista() {
        repositorio.guardar(new ListaDeseos(null, CLIENTE, List.of(new ItemDeseo("p-1", AYER))));

        assertThrows(ProductoNoDisponibleException.class, () -> servicio.anadir(EMAIL, "p-9"));

        assertEquals(List.of(new ItemDeseo("p-1", AYER)),
                repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems());
    }

    // Guarda una copia, como la base de datos, y asigna el id a las listas nuevas
    private static final class RepositorioEnMemoria implements ListaDeseosRepositoryPort {

        private final Map<String, ListaDeseos> listas = new HashMap<>();

        @Override
        public Optional<ListaDeseos> buscarPorCliente(String clienteId) {
            return Optional.ofNullable(listas.get(clienteId));
        }

        @Override
        public ListaDeseos guardar(ListaDeseos lista) {
            String id = lista.getId() != null ? lista.getId() : "l-" + (listas.size() + 1);
            ListaDeseos guardada = new ListaDeseos(id, lista.getClienteId(), lista.getItems());
            listas.put(lista.getClienteId(), guardada);
            return guardada;
        }
    }
}
