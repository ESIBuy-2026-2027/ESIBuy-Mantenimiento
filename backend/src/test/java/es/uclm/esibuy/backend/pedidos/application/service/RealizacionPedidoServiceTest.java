package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.pedidos.application.port.in.RealizarPedidoCommand;
import es.uclm.esibuy.backend.pedidos.application.port.out.CarritoPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.CompraProveedor;
import es.uclm.esibuy.backend.pedidos.application.port.out.ContadorPedidosPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException;
import es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException;
import es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficienteException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
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
class RealizacionPedidoServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-10-29T10:00:00Z");
    private static final DireccionEnvioPedido DIRECCION = new DireccionEnvioPedido("Carlos Cliente",
            "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real", "611222333");

    private final PedidosEnMemoria pedidos = new PedidosEnMemoria();
    private final CarritoFijo carrito = new CarritoFijo();
    private final StockEnMemoria stock = new StockEnMemoria();
    private final ContadorEnMemoria contador = new ContadorEnMemoria();
    // El método de pago mp-1 y la dirección d-1 son del cliente (MetodosPagoPort y DireccionesPort)
    private final RealizacionPedidoService servicio = new RealizacionPedidoService(pedidos, new ClienteFijo(),
            carrito, (email, metodoId) -> "mp-1".equals(metodoId),
            (email, direccionId) -> "d-1".equals(direccionId) ? Optional.of(DIRECCION) : Optional.empty(),
            stock, contador, Clock.fixed(AHORA, ZoneOffset.UTC));

    RealizacionPedidoServiceTest() {
        // TecnoVera (u-2): dos auriculares y un teclado; Otero (u-99): un smartwatch
        carrito.compras.put("u-2", new CompraProveedor("u-2", "TecnoVera", List.of(
                new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91")),
                new LineaPedido("p-2", "Teclado", 1, new BigDecimal("89.50")))));
        carrito.compras.put("u-99", new CompraProveedor("u-99", "Otero", List.of(
                new LineaPedido("p-24", "Smartwatch", 1, new BigDecimal("129.00")))));
        stock.unidades.putAll(Map.of("p-1", 10, "p-2", 1, "p-24", 5));
    }

    private Pedido realizar(String vendedorId) {
        return servicio.realizar(new RealizarPedidoCommand(EMAIL, vendedorId, "d-1", "mp-1"));
    }

    @Test
    @DisplayName("HU10-CP14: el pedido lleva solo los productos de su proveedor, con los precios del carrito")
    void elPedidoLlevaSoloLosProductosDeSuProveedor() {
        Pedido pedido = realizar("u-2");

        assertEquals("c-1", pedido.getClienteId());
        assertEquals("u-2", pedido.getVendedorId());
        assertEquals("TecnoVera", pedido.getNombreComercial());
        assertEquals(List.of("p-1", "p-2"), pedido.getLineas().stream().map(LineaPedido::productoId).toList());
        assertEquals(new BigDecimal("197.32"), pedido.getTotal());
        assertEquals(EstadoPedido.PAGADO, pedido.getEstado());
        assertEquals(AHORA, pedido.getCreatedAt());
        assertEquals(List.of(pedido), pedidos.guardados);
    }

    @Test
    @DisplayName("HU10-CP14: tras crear el pedido, sus productos salen del carrito y los del otro proveedor se quedan")
    void trasCrearElPedidoSusProductosSalenDelCarrito() {
        realizar("u-2");

        assertEquals(List.of("p-1", "p-2"), carrito.quitados);
    }

    @Test
    @DisplayName("HU10-CP13: cada pedido recibe el siguiente número del año en curso")
    void cadaPedidoRecibeElSiguienteNumeroDelAno() {
        Pedido primero = realizar("u-2");
        Pedido segundo = realizar("u-99");

        assertEquals("ESB-2026-000001", primero.getNumeroPedido().valor());
        assertEquals("ESB-2026-000002", segundo.getNumeroPedido().valor());
        assertEquals(List.of(2026, 2026), contador.anios);
    }

    @Test
    @DisplayName("HU10-CP15: crear el pedido descuenta del stock las unidades compradas")
    void crearElPedidoDescuentaElStock() {
        realizar("u-2");

        assertEquals(8, stock.unidades.get("p-1"));
        assertEquals(0, stock.unidades.get("p-2"));
        assertEquals(5, stock.unidades.get("p-24"));
    }

    @Test
    @DisplayName("HU10-CP15: si falta stock de un producto no se crea el pedido y se repone lo ya descontado")
    void siFaltaStockNoSeCreaElPedidoYSeRepone() {
        stock.unidades.put("p-2", 0);

        assertThrows(StockInsuficienteException.class, () -> realizar("u-2"));

        assertEquals(10, stock.unidades.get("p-1"));
        assertTrue(pedidos.guardados.isEmpty());
        assertTrue(carrito.quitados.isEmpty());
    }

    @Test
    @DisplayName("HU10-CP15: si no se puede guardar el pedido se repone todo el stock descontado")
    void siNoSePuedeGuardarSeReponeElStock() {
        pedidos.fallar = true;

        assertThrows(IllegalStateException.class, () -> realizar("u-2"));

        assertEquals(10, stock.unidades.get("p-1"));
        assertEquals(1, stock.unidades.get("p-2"));
        assertTrue(carrito.quitados.isEmpty());
    }

    @Test
    @DisplayName("HU10-CP16: el método de pago tiene que ser del cliente; si no, no se toca nada")
    void elMetodoDePagoTieneQueSerDelCliente() {
        assertThrows(MetodoPagoNoValidoException.class,
                () -> servicio.realizar(new RealizarPedidoCommand(EMAIL, "u-2", "d-1", "mp-ajeno")));

        assertEquals(10, stock.unidades.get("p-1"));
        assertTrue(pedidos.guardados.isEmpty());
    }

    @Test
    @DisplayName("HU10-CP16: la dirección tiene que ser del cliente, y el pedido guarda una copia de sus datos")
    void laDireccionTieneQueSerDelClienteYSeCopia() {
        assertThrows(DireccionNoValidaException.class,
                () -> servicio.realizar(new RealizarPedidoCommand(EMAIL, "u-2", "d-ajena", "mp-1")));
        assertTrue(pedidos.guardados.isEmpty());

        assertEquals(DIRECCION, realizar("u-2").getDireccionEnvio());
    }

    @Test
    @DisplayName("HU10-CP17: sin productos de ese proveedor en el carrito no se crea el pedido")
    void sinProductosDelProveedorNoSeCreaElPedido() {
        assertThrows(CarritoSinProductosDelProveedorException.class, () -> realizar("u-sin-productos"));

        assertTrue(pedidos.guardados.isEmpty());
        assertEquals(List.of(), contador.anios);
    }

    private static final class ClienteFijo implements ClienteEnSesionPort {
        @Override
        public String idPorCorreo(String email) {
            return "c-1";
        }
    }

    private static final class CarritoFijo implements CarritoPort {
        private final Map<String, CompraProveedor> compras = new HashMap<>();
        private final List<String> quitados = new ArrayList<>();

        @Override
        public Optional<CompraProveedor> compraDelProveedor(String emailCliente, String vendedorId) {
            return Optional.ofNullable(compras.get(vendedorId));
        }

        @Override
        public void quitar(String emailCliente, List<String> productoIds) {
            quitados.addAll(productoIds);
        }
    }

    private static final class StockEnMemoria implements ExistenciasPort {
        private final Map<String, Integer> unidades = new HashMap<>();

        @Override
        public boolean descontar(String productoId, int cantidad) {
            int actual = unidades.get(productoId);
            if (actual < cantidad) {
                return false;
            }
            unidades.put(productoId, actual - cantidad);
            return true;
        }

        @Override
        public void reponer(String productoId, int cantidad) {
            unidades.merge(productoId, cantidad, Integer::sum);
        }
    }

    private static final class ContadorEnMemoria implements ContadorPedidosPort {
        private final List<Integer> anios = new ArrayList<>();

        @Override
        public long siguiente(int anio) {
            anios.add(anio);
            return anios.size();
        }
    }

    private static final class PedidosEnMemoria implements PedidoRepositoryPort {
        private final List<Pedido> guardados = new ArrayList<>();
        private boolean fallar;

        @Override
        public Pedido guardar(Pedido pedido) {
            if (fallar) {
                throw new IllegalStateException("MongoDB no responde");
            }
            guardados.add(pedido);
            return pedido;
        }
    }
}
