package es.uclm.esibuy.backend.carrito.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.carrito.application.port.in.CarritoDetallado;
import es.uclm.esibuy.backend.carrito.application.port.in.GrupoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.in.LineaCarritoDetallada;
import es.uclm.esibuy.backend.carrito.application.port.out.CarritoRepositoryPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.carrito.domain.exception.LineaCarritoNoEncontradaException;
import es.uclm.esibuy.backend.carrito.domain.exception.ProductoNoComprableException;
import es.uclm.esibuy.backend.carrito.domain.exception.StockInsuficienteException;
import es.uclm.esibuy.backend.carrito.domain.model.Carrito;
import es.uclm.esibuy.backend.carrito.domain.model.LineaCarrito;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Sin Spring: los puertos de salida se sustituyen por dobles de prueba (clases internas de este test)
class GestionCarritoServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final String CLIENTE = "c-1";
    private static final Instant AHORA = Instant.parse("2026-10-29T10:00:00Z");
    private static final Clock RELOJ = Clock.fixed(AHORA, ZoneOffset.UTC);

    // Dos productos de TecnoVera (u-2) y uno de Otero (u-99)
    private static final ProductoCarrito AURICULARES =
            new ProductoCarrito("p-1", "u-2", "Auriculares", "a.png", new BigDecimal("53.91"), 34);
    private static final ProductoCarrito TECLADO =
            new ProductoCarrito("p-2", "u-2", "Teclado", "t.png", new BigDecimal("89.50"), 3);
    private static final ProductoCarrito RELOJ_DEPORTIVO =
            new ProductoCarrito("p-24", "u-99", "Smartwatch", "s.png", new BigDecimal("129.00"), 10);

    private final RepositorioEnMemoria repositorio = new RepositorioEnMemoria();
    private final ProductosFijos productos = new ProductosFijos();
    private final ClienteFijo cliente = new ClienteFijo();
    // El nombre comercial de cada proveedor sale de esta tabla (VendedoresPort)
    private final GestionCarritoService servicio = new GestionCarritoService(repositorio, cliente, productos,
            vendedorId -> Map.of("u-2", "TecnoVera", "u-99", "Otero").get(vendedorId), RELOJ);

    GestionCarritoServiceTest() {
        productos.anadir(AURICULARES);
        productos.anadir(TECLADO);
        productos.anadir(RELOJ_DEPORTIVO);
    }

    @Test
    @DisplayName("HU9-CP10: añadir guarda el carrito del cliente en sesión con el producto y sus unidades")
    void anadirGuardaElCarritoDelCliente() {
        servicio.anadir(EMAIL, "p-1", 2);
        servicio.anadir(EMAIL, "p-1", 1);

        Carrito guardado = repositorio.buscarPorCliente(CLIENTE).orElseThrow();
        assertEquals(List.of(new LineaCarrito("p-1", 3, AHORA, AHORA)), guardado.getLineas());
    }

    @Test
    @DisplayName("HU9-CP11: no se añaden más unidades que el stock y el carrito no cambia")
    void noSeAnadenMasUnidadesQueElStock() {
        servicio.anadir(EMAIL, "p-2", 2);

        assertThrows(StockInsuficienteException.class, () -> servicio.anadir(EMAIL, "p-2", 2));
        assertEquals(2, repositorio.buscarPorCliente(CLIENTE).orElseThrow().getLineas().get(0).cantidad());
    }

    @Test
    @DisplayName("HU9-CP12: un producto que no se puede comprar no se añade ni se le cambia la cantidad")
    void unProductoQueNoSePuedeComprarNoSeAnade() {
        assertThrows(ProductoNoComprableException.class, () -> servicio.anadir(EMAIL, "no-existe", 1));
        assertThrows(ProductoNoComprableException.class, () -> servicio.fijarCantidad(EMAIL, "no-existe", 1));
        assertTrue(repositorio.buscarPorCliente(CLIENTE).isEmpty());
    }

    @Test
    @DisplayName("HU9-CP13: fijar la cantidad la sustituye; si el producto no está en el carrito, falla")
    void fijarLaCantidadLaSustituye() {
        servicio.anadir(EMAIL, "p-1", 2);

        CarritoDetallado carrito = servicio.fijarCantidad(EMAIL, "p-1", 5);

        assertEquals(5, carrito.grupos().get(0).lineas().get(0).cantidad());
        assertThrows(LineaCarritoNoEncontradaException.class, () -> servicio.fijarCantidad(EMAIL, "p-2", 1));
    }

    @Test
    @DisplayName("HU9-CP14: quitar un producto y vaciar el carrito se guardan")
    void quitarYVaciarSeGuardan() {
        servicio.anadir(EMAIL, "p-1", 1);
        servicio.anadir(EMAIL, "p-24", 1);

        CarritoDetallado sinAuriculares = servicio.quitar(EMAIL, "p-1");
        assertEquals(1, sinAuriculares.totalProductos());

        CarritoDetallado vacio = servicio.vaciar(EMAIL);
        assertTrue(vacio.grupos().isEmpty());
        assertTrue(repositorio.buscarPorCliente(CLIENTE).orElseThrow().getLineas().isEmpty());
    }

    @Test
    @DisplayName("HU9-CP15: el carrito sale agrupado por proveedor, con subtotales y totales calculados")
    void elCarritoSaleAgrupadoPorProveedor() {
        servicio.anadir(EMAIL, "p-1", 2);
        servicio.anadir(EMAIL, "p-24", 1);
        servicio.anadir(EMAIL, "p-2", 1);

        CarritoDetallado carrito = servicio.consultar(EMAIL);

        assertEquals(2, carrito.grupos().size());
        GrupoCarrito tecnovera = carrito.grupos().get(0);
        assertEquals("u-2", tecnovera.vendedorId());
        assertEquals("TecnoVera", tecnovera.nombreComercial());
        assertEquals(new BigDecimal("197.32"), tecnovera.total());
        assertEquals(new LineaCarritoDetallada("p-1", "Auriculares", "a.png", 2, new BigDecimal("53.91"),
                new BigDecimal("107.82"), 34), tecnovera.lineas().get(0));
        assertEquals("p-2", tecnovera.lineas().get(1).productoId());
        GrupoCarrito otero = carrito.grupos().get(1);
        assertEquals("Otero", otero.nombreComercial());
        assertEquals(new BigDecimal("129.00"), otero.total());
        assertEquals(4, carrito.totalProductos());
        assertEquals(new BigDecimal("326.32"), carrito.total());
    }

    @Test
    @DisplayName("HU9-CP15: un cliente sin carrito ve un carrito vacío")
    void unClienteSinCarritoVeUnCarritoVacio() {
        CarritoDetallado carrito = servicio.consultar(EMAIL);

        assertTrue(carrito.grupos().isEmpty());
        assertEquals(0, carrito.totalProductos());
        assertEquals(0, BigDecimal.ZERO.compareTo(carrito.total()));
    }

    @Test
    @DisplayName("HU9-CP18: al consultar, se quitan los productos que ya no se pueden comprar y se guarda el carrito")
    void alConsultarSeLimpiaElCarrito() {
        servicio.anadir(EMAIL, "p-1", 1);
        servicio.anadir(EMAIL, "p-2", 3);
        productos.retirar("p-1");
        productos.anadir(new ProductoCarrito("p-2", "u-2", "Teclado", "t.png", new BigDecimal("89.50"), 1));

        CarritoDetallado carrito = servicio.consultar(EMAIL);

        assertEquals(1, carrito.totalProductos());
        assertEquals("p-2", carrito.grupos().get(0).lineas().get(0).productoId());
        assertEquals(List.of(new LineaCarrito("p-2", 1, AHORA, AHORA)),
                repositorio.buscarPorCliente(CLIENTE).orElseThrow().getLineas());
    }

    @Test
    @DisplayName("HU9-CP19: el precio de cada línea es el del cliente: se pide el de premium si lo es")
    void elPrecioEsElDelCliente() {
        cliente.premium = true;

        servicio.anadir(EMAIL, "p-1", 1);

        assertEquals(List.of(true), productos.premiumPedidos.stream().distinct().toList());
    }

    private static final class RepositorioEnMemoria implements CarritoRepositoryPort {
        private final Map<String, Carrito> porCliente = new HashMap<>();

        @Override
        public Optional<Carrito> buscarPorCliente(String clienteId) {
            return Optional.ofNullable(porCliente.get(clienteId));
        }

        @Override
        public Carrito guardar(Carrito carrito) {
            Carrito guardado = new Carrito("carrito-" + carrito.getClienteId(), carrito.getClienteId(),
                    carrito.getLineas());
            porCliente.put(carrito.getClienteId(), guardado);
            return guardado;
        }
    }

    private static final class ClienteFijo implements ClienteEnSesionPort {
        private boolean premium;

        @Override
        public String idPorCorreo(String email) {
            return CLIENTE;
        }

        @Override
        public boolean esPremium(String email) {
            return premium;
        }
    }

    // Los productos que se pueden comprar; uno retirado deja de estar
    private static final class ProductosFijos implements ProductosPort {
        private final Map<String, ProductoCarrito> comprables = new HashMap<>();
        private final List<Boolean> premiumPedidos = new ArrayList<>();

        void anadir(ProductoCarrito producto) {
            comprables.put(producto.id(), producto);
        }

        void retirar(String productoId) {
            comprables.remove(productoId);
        }

        @Override
        public Optional<ProductoCarrito> consultarComprable(String productoId, boolean clientePremium) {
            premiumPedidos.add(clientePremium);
            return Optional.ofNullable(comprables.get(productoId));
        }
    }
}
