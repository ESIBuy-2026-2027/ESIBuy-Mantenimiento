package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

@ExtendWith(MockitoExtension.class)
class ProductosConPedidosServiceTest {

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    @Test
    @DisplayName("HU47-CP02: al eliminar la cuenta se conservan como no disponibles los productos con pedidos y se eliminan los demás junto con sus valoraciones")
    void devuelveLosProductosDePedidosDelVendedor() {
        Pedido pedido = mock(Pedido.class);
        when(pedido.getLineas()).thenReturn(List.of(
                new LineaPedido("p-1", "Producto", 1, java.math.BigDecimal.TEN),
                new LineaPedido("p-2", "Otro", 1, java.math.BigDecimal.ONE)));
        when(pedidoRepository.buscarPorVendedor("v-1")).thenReturn(List.of(pedido));

        assertEquals(Set.of("p-1", "p-2"), new ProductosConPedidosService(pedidoRepository)
                .buscarDeVendedor("v-1"));
    }
}
