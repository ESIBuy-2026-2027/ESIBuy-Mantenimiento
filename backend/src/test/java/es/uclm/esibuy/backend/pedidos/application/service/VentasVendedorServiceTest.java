package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Ventas de un vendedor para sus estadísticas (HU17, T280). Sin Spring: el repositorio se sustituye por un doble de
// prueba (clase interna de este test).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP06, propuesto.
class VentasVendedorServiceTest {

    private final PedidosPorVendedor pedidos = new PedidosPorVendedor();
    private final VentasVendedorService servicio = new VentasVendedorService(pedidos);

    @Test
    @DisplayName("HU17-CP06: las ventas del vendedor son las líneas de sus pedidos, con el importe de cada una")
    void lasVentasSonLasLineasDeSusPedidos() {
        pedidos.delVendedor.put("u-2", List.of(pedido("o-1", EstadoPedido.PAGADO,
                new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("20.00")),
                new LineaPedido("p-2", "Teclado", 1, new BigDecimal("89.50")))));

        List<VentaProducto> ventas = servicio.ventasDe("u-2");

        assertEquals(List.of(
                new VentaProducto("o-1", "p-1", "Auriculares", 2, new BigDecimal("40.00")),
                new VentaProducto("o-1", "p-2", "Teclado", 1, new BigDecimal("89.50"))), ventas);
        assertEquals(List.of("u-2"), pedidos.consultas);
    }

    @Test
    @DisplayName("HU17-CP06: los pedidos cancelados no cuentan como ventas; los demás estados sí")
    void losPedidosCanceladosNoCuentan() {
        LineaPedido linea = new LineaPedido("p-1", "Auriculares", 1, new BigDecimal("10.00"));
        pedidos.delVendedor.put("u-2", List.of(
                pedido("o-1", EstadoPedido.CANCELADO, linea),
                pedido("o-2", EstadoPedido.PAGADO, linea),
                pedido("o-3", EstadoPedido.PREPARANDO, linea),
                pedido("o-4", EstadoPedido.ENVIADO, linea),
                pedido("o-5", EstadoPedido.ENTREGADO, linea)));

        List<String> pedidosVendidos = servicio.ventasDe("u-2").stream().map(VentaProducto::pedidoId).toList();

        assertEquals(List.of("o-2", "o-3", "o-4", "o-5"), pedidosVendidos);
    }

    @Test
    @DisplayName("HU17-CP06: un vendedor sin pedidos no tiene ventas")
    void sinPedidosNoHayVentas() {
        assertEquals(List.of(), servicio.ventasDe("u-sin-pedidos"));
    }

    private static Pedido pedido(String id, EstadoPedido estado, LineaPedido... lineas) {
        return new Pedido(id, new NumeroPedido("ESB-2026-000001"), "c-1", "u-2", "TecnoVera", estado,
                List.of(lineas), new DireccionEnvioPedido("Carlos Cliente", "Calle Mayor 1", null, "13001",
                        "Ciudad Real", "Ciudad Real", "611222333"),
                "mp-1", Instant.parse("2026-10-29T10:00:00Z"));
    }

    // Los pedidos de cada vendedor; apunta cada consulta que recibe
    private static final class PedidosPorVendedor implements PedidoRepositoryPort {
        private final Map<String, List<Pedido>> delVendedor = new HashMap<>();
        private final List<String> consultas = new ArrayList<>();

        @Override
        public List<Pedido> buscarPorVendedor(String vendedorId) {
            consultas.add(vendedorId);
            return delVendedor.getOrDefault(vendedorId, List.of());
        }

        @Override
        public Pedido guardar(Pedido pedido) {
            throw new UnsupportedOperationException("Las ventas del vendedor no guardan pedidos");
        }

        @Override
        public boolean existePagadoConProducto(String clienteId, String productoId) {
            throw new UnsupportedOperationException("Las ventas del vendedor no consultan compras");
        }

        @Override
        public List<Pedido> buscarPorCliente(String clienteId) {
            throw new UnsupportedOperationException("Las ventas del vendedor no consultan pedidos de clientes");
        }

        @Override
        public Optional<Pedido> buscarPorId(String id) {
            throw new UnsupportedOperationException("Las ventas del vendedor no consultan un pedido");
        }
    }
}
