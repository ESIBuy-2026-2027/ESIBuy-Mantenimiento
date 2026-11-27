package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

class VentasPlataformaServiceTest {

    @Test
    @DisplayName("HU519-CP02: no cuenta las líneas de pedidos cancelados en las ventas de la plataforma")
    void excluyePedidosCancelados() {
        LineaPedido linea = new LineaPedido("p-1", "v-1", "Teclado", 2, new BigDecimal("10.00"));
        PedidoRepositoryDoble pedidos = new PedidoRepositoryDoble(List.of(
                pedido("o-1", EstadoPedido.CANCELADO, linea),
                pedido("o-2", EstadoPedido.PAGADO, linea),
                pedido("o-3", EstadoPedido.PAGADO,
                        new LineaPedido("p-2", "v-1", "Ratón", 1, new BigDecimal("5.00")))));

        List<VentaProducto> ventas = new VentasPlataformaService(pedidos).consultar();

        assertEquals(List.of(
                new VentaProducto("o-2", "p-1", "Teclado", 2, new BigDecimal("20.00")),
                new VentaProducto("o-3", "p-2", "Ratón", 1, new BigDecimal("5.00"))), ventas);
    }

    private static Pedido pedido(String id, EstadoPedido estado, LineaPedido linea) {
        return new Pedido(new DatosPedido(id, new NumeroPedido("ESB-2026-000001"), "c-1", "v-1", "Tienda",
                estado, List.of(linea), new DireccionEnvioPedido("Cliente", "Calle 1", null, "13001", "Ciudad Real",
                        "Ciudad Real", "611222333"),
                "mp-1", Instant.parse("2026-10-29T10:00:00Z")));
    }

    private static final class PedidoRepositoryDoble implements PedidoRepositoryPort {
        private final List<Pedido> todos;

        private PedidoRepositoryDoble(List<Pedido> todos) {
            this.todos = todos;
        }

        @Override
        public List<Pedido> buscarTodos() {
            return todos;
        }

        @Override
        public Pedido guardar(Pedido pedido) {
            throw new UnsupportedOperationException();
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
            throw new UnsupportedOperationException();
        }

        @Override
        public List<Pedido> buscarPorVendedor(String vendedorId) {
            throw new UnsupportedOperationException();
        }
    }
}
