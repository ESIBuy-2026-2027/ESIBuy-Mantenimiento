package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEntregableException;
import es.uclm.esibuy.backend.pedidos.domain.model.DatosPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

// Seguimiento del envío del cliente, sin Spring: los puertos de salida son dobles de prueba y el reloj es fijo.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU11-CP24 a CP26, propuestos.
class SeguimientoEnvioServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";
    private static final Instant AHORA = Instant.parse("2026-11-05T12:30:00Z");

    private final PedidoRepositoryPort pedidoRepository = mock(PedidoRepositoryPort.class);
    private final ClienteEnSesionPort clienteEnSesion = mock(ClienteEnSesionPort.class);
    private final SeguimientoEnvioService servicio = new SeguimientoEnvioService(pedidoRepository, clienteEnSesion,
            Clock.fixed(AHORA, ZoneOffset.UTC));

    private static Pedido pedido(String clienteId, EstadoPedido estado) {
        return new Pedido(new DatosPedido(
                "o-1",
                new NumeroPedido("ESB-2026-000001"),
                clienteId,
                "u-2",
                "TecnoVera",
                estado,
                List.of(new LineaPedido("p-1", "u-2", "Auriculares", 1, new BigDecimal("10.00"))),
                new DireccionEnvioPedido("Carlos", "Calle Mayor 1", null, "13001", "Ciudad Real", "Ciudad Real",
                        "611222333"),
                "mp-1",
                Instant.parse("2026-10-29T10:00:00Z")));
    }

    private void clienteEnSesion(String clienteId) {
        when(clienteEnSesion.idPorCorreo(EMAIL)).thenReturn(clienteId);
    }

    @Test
    @DisplayName("HU11-CP24: consultar el seguimiento devuelve el pedido del cliente de la sesión, buscado por su id")
    void consultarElSeguimientoDevuelveElPedidoDelCliente() {
        Pedido pedido = pedido("c-1", EstadoPedido.ENVIADO);
        clienteEnSesion("c-1");
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido));

        assertSame(pedido, servicio.consultarSeguimiento(EMAIL, "o-1"));
    }

    @Test
    @DisplayName("HU11-CP24: el seguimiento de un pedido ajeno o inexistente se trata igual y no revela nada")
    void elSeguimientoDeUnPedidoAjenoOInexistenteNoLoRevela() {
        clienteEnSesion("c-1");
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido("c-2", EstadoPedido.ENVIADO)));
        when(pedidoRepository.buscarPorId("o-9")).thenReturn(Optional.empty());

        assertThrows(PedidoNoEncontradoException.class, () -> servicio.consultarSeguimiento(EMAIL, "o-1"));
        assertThrows(PedidoNoEncontradoException.class, () -> servicio.consultarSeguimiento(EMAIL, "o-9"));
    }

    @Test
    @DisplayName("HU11-CP25: confirmar la entrega de un pedido enviado lo guarda entregado con la hora del reloj")
    void confirmarLaEntregaGuardaElPedidoEntregado() {
        Pedido pedido = pedido("c-1", EstadoPedido.ENVIADO);
        clienteEnSesion("c-1");
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.guardar(pedido)).thenReturn(pedido);

        Pedido entregado = servicio.confirmarEntrega(EMAIL, "o-1");

        assertEquals(EstadoPedido.ENTREGADO, entregado.getEstado());
        assertEquals(AHORA, entregado.getFechaEntrega());
        verify(pedidoRepository).guardar(pedido);
    }

    @Test
    @DisplayName("HU11-CP26: un pedido que no está enviado, o ya entregado, no se confirma ni se guarda")
    void unPedidoQueNoEstaEnviadoNoSeConfirmaNiSeGuarda() {
        clienteEnSesion("c-1");
        for (EstadoPedido estado : List.of(EstadoPedido.PAGADO, EstadoPedido.PREPARANDO, EstadoPedido.ENTREGADO,
                EstadoPedido.CANCELADO)) {
            when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido("c-1", estado)));

            assertThrows(PedidoNoEntregableException.class, () -> servicio.confirmarEntrega(EMAIL, "o-1"));
        }

        verify(pedidoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU11-CP26: un pedido ajeno o inexistente no se confirma y se trata como inexistente")
    void unPedidoAjenoNoSeConfirma() {
        clienteEnSesion("c-1");
        when(pedidoRepository.buscarPorId("o-1")).thenReturn(Optional.of(pedido("c-2", EstadoPedido.ENVIADO)));

        assertThrows(PedidoNoEncontradoException.class, () -> servicio.confirmarEntrega(EMAIL, "o-1"));
        verify(pedidoRepository, never()).guardar(any());
    }
}
