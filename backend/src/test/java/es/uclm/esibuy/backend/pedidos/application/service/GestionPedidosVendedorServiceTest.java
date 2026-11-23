package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

class GestionPedidosVendedorServiceTest {

    private final PedidosEnMemoria pedidos = new PedidosEnMemoria();
    private final GestionPedidosVendedorService servicio = new GestionPedidosVendedorService(pedidos,
            email -> "u-2");

    @Test
    @DisplayName("gestion-pedidos-vendedor: lista solo los pedidos del vendedor de la sesión")
    void listaSoloLosPedidosDelVendedor() {
        pedidos.porVendedor.put("u-2", List.of(pedido("o-1", "u-2", EstadoPedido.PAGADO)));
        pedidos.porVendedor.put("u-99", List.of(pedido("o-2", "u-99", EstadoPedido.PAGADO)));

        List<Pedido> resultado = servicio.listarPedidos("vendedor@esibuy.test");

        assertEquals(List.of("o-1"), resultado.stream().map(Pedido::getId).toList());
        assertEquals(List.of("u-2"), pedidos.consultasVendedor);
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: devuelve el detalle de un pedido propio")
    void devuelveElDetalleDePedidoPropio() {
        Pedido pedido = pedido("o-1", "u-2", EstadoPedido.ENVIADO);
        pedidos.guardados.put(pedido.getId(), pedido);

        Pedido resultado = servicio.consultarPedido("vendedor@esibuy.test", "o-1");

        assertEquals("o-1", resultado.getId());
        assertEquals(EstadoPedido.ENVIADO, resultado.getEstado());
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: un pedido ajeno o inexistente no se devuelve")
    void unPedidoAjenoNoSeDevuelve() {
        pedidos.guardados.put("o-2", pedido("o-2", "u-99", EstadoPedido.PAGADO));

        assertThrows(PedidoNoEncontradoException.class,
                () -> servicio.consultarPedido("vendedor@esibuy.test", "o-2"));
        assertThrows(PedidoNoEncontradoException.class,
                () -> servicio.consultarPedido("vendedor@esibuy.test", "o-inexistente"));
    }

    @Test
    @DisplayName("gestion-pedidos-vendedor: avanza el estado y guarda el pedido")
    void avanzaYGuardaElEstado() {
        Pedido pedido = pedido("o-1", "u-2", EstadoPedido.PAGADO);
        pedidos.guardados.put(pedido.getId(), pedido);

        Pedido resultado = servicio.avanzarEstado("vendedor@esibuy.test", "o-1");

        assertEquals(EstadoPedido.PREPARANDO, resultado.getEstado());
        assertEquals(pedido, pedidos.guardados.get("o-1"));
        assertEquals(1, pedidos.guardadosCount);
    }

    private static Pedido pedido(String id, String vendedorId, EstadoPedido estado) {
        return new Pedido(new DatosPedido(
                id,
                new NumeroPedido("ESB-2026-000001"),
                "c-1",
                vendedorId,
                "TecnoVera",
                estado,
                List.of(new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91"))),
                new DireccionEnvioPedido("Carlos Cliente", "Calle Mayor 1", null, "13001",
                        "Ciudad Real", "Ciudad Real", "611222333"),
                "mp-1",
                Instant.parse("2026-10-29T10:00:00Z")));
    }

    private static final class PedidosEnMemoria implements PedidoRepositoryPort {
        private final Map<String, List<Pedido>> porVendedor = new java.util.HashMap<>();
        private final Map<String, Pedido> guardados = new java.util.HashMap<>();
        private final List<String> consultasVendedor = new ArrayList<>();
        private int guardadosCount;

        @Override
        public Pedido guardar(Pedido pedido) {
            guardadosCount++;
            guardados.put(pedido.getId(), pedido);
            return pedido;
        }

        @Override
        public boolean existePagadoConProducto(String clienteId, String productoId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Pedido> buscarPorCliente(String clienteId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Pedido> buscarPorId(String id) {
            return Optional.ofNullable(guardados.get(id));
        }

        @Override
        public List<Pedido> buscarPorVendedor(String vendedorId) {
            consultasVendedor.add(vendedorId);
            return porVendedor.getOrDefault(vendedorId, List.of());
        }
    }
}
