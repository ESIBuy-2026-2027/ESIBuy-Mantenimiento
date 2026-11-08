package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import es.uclm.esibuy.backend.pedidos.application.port.in.CancelacionPedidoUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoCancelableException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

// Cancelación de un pedido por el cliente (HU12, T375). Criterios de aceptación: solo se cancela si el pedido aún no
// se ha enviado, y el stock se repone al cancelar. Sin Spring: los puertos de salida se sustituyen por dobles de prueba.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU12-CP03 a CP06, propuestos.
@ExtendWith(MockitoExtension.class)
class CancelacionPedidoServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";

    @Mock
    private PedidoRepositoryPort pedidoRepository;

    @Mock
    private ClienteEnSesionPort clienteEnSesion;

    @Mock
    private ExistenciasPort existencias;

    private CancelacionPedidoUseCase cancelacion; // Tipado como el puerto de entrada

    @BeforeEach
    void crearServicio() {
        cancelacion = new CancelacionPedidoService(pedidoRepository, clienteEnSesion, existencias);
    }

    // Pedido de dos productos: 2 auriculares y 1 teclado
    private static Pedido pedido(String clienteId, EstadoPedido estado) {
        return new Pedido("o-1", new NumeroPedido("ESB-2026-000001"), clienteId, "u-2", "TecnoVera", estado,
                List.of(new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91")),
                        new LineaPedido("p-2", "Teclado", 1, new BigDecimal("89.50"))),
                new DireccionEnvioPedido("Carlos Cliente", "Calle Mayor 1", null, "13001", "Ciudad Real",
                        "Ciudad Real", "611222333"),
                "mp-1", Instant.parse("2026-10-29T10:00:00Z"));
    }

    private void elPedidoEs(Pedido pedido) {
        when(clienteEnSesion.idPorCorreo(EMAIL)).thenReturn("c-1");
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.ofNullable(pedido));
    }

    @Test
    @DisplayName("HU12-CP03: cancelar un pedido del cliente lo guarda cancelado y lo devuelve")
    void cancelarGuardaElPedidoCanceladoYLoDevuelve() {
        elPedidoEs(pedido("c-1", EstadoPedido.PAGADO));

        Pedido cancelado = cancelacion.cancelarPedido(EMAIL, "o-1");

        assertEquals(EstadoPedido.CANCELADO, cancelado.getEstado());
        ArgumentCaptor<Pedido> guardado = ArgumentCaptor.forClass(Pedido.class);
        verify(pedidoRepository).guardar(guardado.capture());
        assertEquals("o-1", guardado.getValue().getId());
        assertEquals(EstadoPedido.CANCELADO, guardado.getValue().getEstado());
    }

    @Test
    @DisplayName("HU12-CP04: al cancelar se repone el stock de cada producto, después de guardar la cancelación")
    void cancelarReponeElStockDespuesDeGuardar() {
        elPedidoEs(pedido("c-1", EstadoPedido.PREPARANDO));

        cancelacion.cancelarPedido(EMAIL, "o-1");

        InOrder orden = inOrder(pedidoRepository, existencias);
        orden.verify(pedidoRepository).guardar(any());
        orden.verify(existencias).reponer("p-1", 2);
        orden.verify(existencias).reponer("p-2", 1);
    }

    @Test
    @DisplayName("HU12-CP05: un pedido de otro cliente o que no existe no se cancela y se trata como inexistente")
    void unPedidoAjenoOInexistenteNoSeCancela() {
        elPedidoEs(pedido("c-2", EstadoPedido.PAGADO));
        assertThrows(PedidoNoEncontradoException.class, () -> cancelacion.cancelarPedido(EMAIL, "o-1"));

        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.empty());
        assertThrows(PedidoNoEncontradoException.class, () -> cancelacion.cancelarPedido(EMAIL, "o-1"));

        verify(pedidoRepository, never()).guardar(any());
        verify(existencias, never()).reponer(anyString(), anyInt());
    }

    @Test
    @DisplayName("HU12-CP06: un pedido enviado o ya cancelado no se cancela, ni se guarda ni se repone stock")
    void unPedidoNoCancelableNoCambiaNada() {
        elPedidoEs(pedido("c-1", EstadoPedido.ENVIADO));
        assertThrows(PedidoNoCancelableException.class, () -> cancelacion.cancelarPedido(EMAIL, "o-1"));

        // Si se pudiera cancelar dos veces, el stock se repondría dos veces
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido("c-1", EstadoPedido.CANCELADO)));
        assertThrows(PedidoNoCancelableException.class, () -> cancelacion.cancelarPedido(EMAIL, "o-1"));

        verify(pedidoRepository, never()).guardar(any());
        verify(existencias, never()).reponer(anyString(), anyInt());
    }
}
