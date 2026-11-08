package es.uclm.esibuy.backend.deseos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.deseos.application.port.in.DeseoDetallado;
import es.uclm.esibuy.backend.deseos.application.port.in.ListaDeseosDetallada;
import es.uclm.esibuy.backend.deseos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ListaDeseosRepositoryPort;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductoDeseado;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.deseos.domain.exception.ProductoNoDisponibleException;
import es.uclm.esibuy.backend.deseos.domain.model.ItemDeseo;
import es.uclm.esibuy.backend.deseos.domain.model.ListaDeseos;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba
class GestionListaDeseosServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final String CLIENTE = "c-1";
    private static final Instant AYER = Instant.parse("2026-11-03T10:00:00Z");
    private static final Instant AHORA = Instant.parse("2026-11-04T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    private static final BigDecimal PRECIO_ESTANDAR = new BigDecimal("45.00");
    private static final BigDecimal PRECIO_PREMIUM = new BigDecimal("40.00");

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ClienteFijo cliente = new ClienteFijo();
    // p-1 y p-2 están disponibles, p-3 está desactivado y cualquier otro no existe
    private final ProductosFijos productos = new ProductosFijos(Map.of(
            "p-1", producto("p-1", "Teclado", true),
            "p-2", producto("p-2", "Ratón", true),
            "p-3", producto("p-3", "Webcam", false)));
    private final GestionListaDeseosService servicio =
            new GestionListaDeseosService(repositorio, cliente, productos, RELOJ);

    private static ProductoDeseado producto(String id, String nombre, boolean disponible) {
        return new ProductoDeseado(id, nombre, id + ".png", PRECIO_ESTANDAR, 7, disponible);
    }

    private void listaGuardadaCon(String... productosId) {
        repositorio.guardar(new ListaDeseos(null, CLIENTE, Arrays.stream(productosId)
                .map(productoId -> new ItemDeseo(productoId, AYER)).toList()));
    }

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

    @Test
    @DisplayName("HU14-CP11: consultar devuelve cada producto con sus datos actuales y su fecha, en orden de añadido")
    void consultarDevuelveCadaProductoConSusDatosActuales() {
        listaGuardadaCon("p-2", "p-1");

        ListaDeseosDetallada lista = servicio.consultar(EMAIL);

        assertEquals(List.of(
                new DeseoDetallado("p-2", "Ratón", "p-2.png", PRECIO_ESTANDAR, 7, true, AYER),
                new DeseoDetallado("p-1", "Teclado", "p-1.png", PRECIO_ESTANDAR, 7, true, AYER)), lista.items());
    }

    @Test
    @DisplayName("HU14-CP11: un cliente sin lista recibe una vacía y no se guarda nada")
    void sinListaDevuelveUnaVaciaSinGuardarla() {
        assertTrue(servicio.consultar(EMAIL).items().isEmpty());

        assertTrue(repositorio.listas.isEmpty());
    }

    @Test
    @DisplayName("HU14-CP12: un producto desactivado sigue en la lista, marcado como no disponible")
    void unProductoDesactivadoSigueEnLaLista() {
        listaGuardadaCon("p-3");

        List<DeseoDetallado> items = servicio.consultar(EMAIL).items();

        assertEquals(1, items.size());
        assertFalse(items.get(0).disponible());
        assertEquals(1, repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems().size());
    }

    @Test
    @DisplayName("HU14-CP13: un producto eliminado desaparece de la respuesta y la lista se guarda sin él")
    void unProductoEliminadoDesapareceYLaListaSeGuarda() {
        listaGuardadaCon("p-1", "p-9");

        List<DeseoDetallado> items = servicio.consultar(EMAIL).items();

        assertEquals(List.of("p-1"), items.stream().map(DeseoDetallado::productoId).toList());
        assertEquals(List.of(new ItemDeseo("p-1", AYER)),
                repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems());
    }

    @Test
    @DisplayName("HU14-CP13: si no hay productos eliminados, consultar no guarda la lista")
    void sinProductosEliminadosNoSeGuardaLaLista() {
        listaGuardadaCon("p-1");
        int guardadosAntes = repositorio.guardados;

        servicio.consultar(EMAIL);

        assertEquals(guardadosAntes, repositorio.guardados);
    }

    @Test
    @DisplayName("HU14-CP14: el precio de cada producto es el que le toca al cliente, estándar o premium")
    void elPrecioEsElQueLeTocaAlCliente() {
        listaGuardadaCon("p-1");

        assertEquals(PRECIO_ESTANDAR, servicio.consultar(EMAIL).items().get(0).precioFinal());

        cliente.premium = true;
        assertEquals(PRECIO_PREMIUM, servicio.consultar(EMAIL).items().get(0).precioFinal());
    }

    @Test
    @DisplayName("HU14-CP25: quitar un producto lo saca de la lista, la guarda y devuelve la lista actualizada")
    void quitarUnProductoDevuelveLaListaActualizada() {
        listaGuardadaCon("p-1", "p-2");

        ListaDeseosDetallada lista = servicio.quitar(EMAIL, "p-1");

        assertEquals(List.of("p-2"), lista.items().stream().map(DeseoDetallado::productoId).toList());
        assertEquals(List.of(new ItemDeseo("p-2", AYER)),
                repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems());
    }

    @Test
    @DisplayName("HU14-CP26: quitar un producto que no está no es un error y no se guarda nada")
    void quitarUnProductoQueNoEstaNoGuardaNada() {
        listaGuardadaCon("p-1");
        int guardadosAntes = repositorio.guardados;

        ListaDeseosDetallada lista = servicio.quitar(EMAIL, "p-2");

        assertEquals(List.of("p-1"), lista.items().stream().map(DeseoDetallado::productoId).toList());
        assertEquals(guardadosAntes, repositorio.guardados);
    }

    @Test
    @DisplayName("HU14-CP26: quitar con un cliente sin lista no es un error y no se crea la lista")
    void quitarSinListaNoLaCrea() {
        assertTrue(servicio.quitar(EMAIL, "p-1").items().isEmpty());

        assertTrue(repositorio.listas.isEmpty());
    }

    @Test
    @DisplayName("HU14-CP27: un producto desactivado también se puede quitar")
    void unProductoDesactivadoTambienSeQuita() {
        listaGuardadaCon("p-3");

        servicio.quitar(EMAIL, "p-3");

        assertTrue(repositorio.buscarPorCliente(CLIENTE).orElseThrow().getItems().isEmpty());
    }

    @Test
    @DisplayName("HU14-CP42: eliminar la lista del cliente la borra y no toca la de otro cliente")
    void eliminarBorraLaListaDelCliente() {
        listaGuardadaCon("p-1");
        repositorio.guardar(new ListaDeseos(null, "c-2", List.of(new ItemDeseo("p-2", AYER))));

        servicio.eliminar(EMAIL);

        assertTrue(repositorio.buscarPorCliente(CLIENTE).isEmpty());
        assertTrue(repositorio.buscarPorCliente("c-2").isPresent());
    }

    @Test
    @DisplayName("HU14-CP42: eliminar la lista de un cliente que no tiene no es un error")
    void eliminarSinListaNoEsUnError() {
        servicio.eliminar(EMAIL);

        assertTrue(repositorio.buscarPorCliente(CLIENTE).isEmpty());
    }

    private static final class ClienteFijo implements ClienteEnSesionPort {

        private boolean premium;

        @Override
        public String idPorCorreo(String email) {
            return Map.of(EMAIL, CLIENTE).get(email);
        }

        @Override
        public boolean esPremium(String email) {
            return premium;
        }
    }

    // El precio premium es distinto, para comprobar que el servicio pide el que le toca al cliente
    private record ProductosFijos(Map<String, ProductoDeseado> productos) implements ProductosPort {

        @Override
        public boolean estaDisponible(String productoId) {
            return productos.containsKey(productoId) && productos.get(productoId).disponible();
        }

        @Override
        public Optional<ProductoDeseado> consultarResumen(String productoId, boolean clientePremium) {
            return Optional.ofNullable(productos.get(productoId))
                    .map(p -> new ProductoDeseado(p.id(), p.nombre(), p.imagen(),
                            clientePremium ? PRECIO_PREMIUM : p.precioFinal(), p.stock(), p.disponible()));
        }
    }

    // Guarda una copia, como la base de datos, y asigna el id a las listas nuevas
    private static final class RepositorioEnMemoria implements ListaDeseosRepositoryPort {

        private final Map<String, ListaDeseos> listas = new HashMap<>();
        private int guardados;

        @Override
        public Optional<ListaDeseos> buscarPorCliente(String clienteId) {
            return Optional.ofNullable(listas.get(clienteId));
        }

        @Override
        public ListaDeseos guardar(ListaDeseos lista) {
            String id = lista.getId() != null ? lista.getId() : "l-" + (listas.size() + 1);
            ListaDeseos guardada = new ListaDeseos(id, lista.getClienteId(), lista.getItems());
            listas.put(lista.getClienteId(), guardada);
            guardados++;
            return guardada;
        }

        @Override
        public void eliminarPorCliente(String clienteId) {
            listas.remove(clienteId);
        }
    }
}
