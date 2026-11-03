package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.exception.PedidoNoEncontradoException;
import es.uclm.esibuy.backend.pedidos.domain.model.DireccionEnvioPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.LineaPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.NumeroPedido;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Consulta de las compras de un cliente (HU13, T370): valoraciones pregunta a pedidos si el cliente ha comprado
// un producto antes de dejarle valorarlo. Sin Spring: los puertos de salida se sustituyen por dobles de prueba
// (clases internas de este test).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP06, propuesto.
// También el historial y el detalle de los pedidos del cliente (HU11, T378): casos HU11-CP01 a CP03, propuestos.
class ConsultaComprasServiceTest {

    private static final String EMAIL = "cliente@esibuy.test";

    private final PedidosPagadosFijos pedidos = new PedidosPagadosFijos();
    private final ConsultaComprasService servicio = new ConsultaComprasService(pedidos, new ClienteFijo());

    @Test
    @DisplayName("HU13-CP06: el cliente ha comprado un producto si tiene un pedido pagado con él")
    void haCompradoSiTieneUnPedidoPagadoConElProducto() {
        pedidos.pagados.add(par("c-1", "p-1"));

        assertTrue(servicio.haComprado(EMAIL, "p-1"));
    }

    @Test
    @DisplayName("HU13-CP06: el cliente no ha comprado un producto si no tiene ningún pedido pagado con él")
    void noHaCompradoSiNoTienePedidoPagadoConElProducto() {
        pedidos.pagados.add(par("c-1", "p-1"));
        pedidos.pagados.add(par("c-2", "p-2"));

        assertFalse(servicio.haComprado(EMAIL, "p-2"));
    }

    @Test
    @DisplayName("HU13-CP06: la consulta de pedidos se hace con el id del cliente, no con su correo")
    void laConsultaSeHaceConElIdDelCliente() {
        servicio.haComprado(EMAIL, "p-1");

        assertEquals(List.of(par("c-1", "p-1")), pedidos.consultas);
    }

    // --- Historial y detalle de los pedidos (HU11) -------------------------------------------------------------

    @Test
    @DisplayName("HU11-CP01: el historial devuelve los pedidos del cliente de la sesión, del más reciente al más antiguo")
    void listarPedidosDevuelveLosDelClienteEnElOrdenDelRepositorio() {
        Pedido reciente = pedidoDe("o-2", "c-1");
        Pedido antiguo = pedidoDe("o-1", "c-1");
        pedidos.delCliente.put("c-1", List.of(reciente, antiguo));

        List<Pedido> historial = servicio.listarPedidos(EMAIL);

        assertEquals(List.of(reciente, antiguo), historial);
        assertEquals(List.of("c-1"), pedidos.consultasPorCliente);
    }

    @Test
    @DisplayName("HU11-CP01: un cliente sin pedidos tiene el historial vacío")
    void listarPedidosSinPedidosDevuelveUnaListaVacia() {
        assertEquals(List.of(), servicio.listarPedidos(EMAIL));
    }

    @Test
    @DisplayName("HU11-CP02: el detalle devuelve el pedido del cliente con sus productos, precios y estado")
    void consultarPedidoDevuelveElPedidoDelCliente() {
        Pedido pedido = pedidoDe("o-1", "c-1");
        pedidos.porId.put("o-1", pedido);

        Pedido detalle = servicio.consultarPedido(EMAIL, "o-1");

        assertSame(pedido, detalle);
    }

    @Test
    @DisplayName("HU11-CP03: el pedido de otro cliente se trata como si no existiera, igual que uno inexistente")
    void consultarPedidoAjenoOInexistenteNoLoRevela() {
        pedidos.porId.put("o-9", pedidoDe("o-9", "c-2"));

        assertThrows(PedidoNoEncontradoException.class, () -> servicio.consultarPedido(EMAIL, "o-9"));
        assertThrows(PedidoNoEncontradoException.class, () -> servicio.consultarPedido(EMAIL, "o-no-existe"));
    }

    private static Pedido pedidoDe(String id, String clienteId) {
        return new Pedido(id, new NumeroPedido("ESB-2026-000001"), clienteId, "u-2", "TecnoVera",
                EstadoPedido.PAGADO, List.of(new LineaPedido("p-1", "Auriculares", 2, new BigDecimal("53.91"))),
                new DireccionEnvioPedido("Carlos Cliente", "Calle Mayor 1", null, "13001", "Ciudad Real",
                        "Ciudad Real", "611222333"),
                "mp-1", Instant.parse("2026-10-29T10:00:00Z"));
    }

    private static String par(String clienteId, String productoId) {
        return clienteId + "|" + productoId;
    }

    // El correo de la sesión corresponde al cliente c-1
    private static final class ClienteFijo implements ClienteEnSesionPort {
        @Override
        public String idPorCorreo(String email) {
            return EMAIL.equals(email) ? "c-1" : "c-desconocido";
        }
    }

    // Pedidos pagados guardados como pares cliente|producto; apunta cada consulta que recibe.
    // Para el historial (HU11), los pedidos de cada cliente ya ordenados y los pedidos por id.
    private static final class PedidosPagadosFijos implements PedidoRepositoryPort {
        private final Set<String> pagados = new HashSet<>();
        private final List<String> consultas = new ArrayList<>();
        private final Map<String, List<Pedido>> delCliente = new HashMap<>();
        private final Map<String, Pedido> porId = new HashMap<>();
        private final List<String> consultasPorCliente = new ArrayList<>();

        @Override
        public Pedido guardar(Pedido pedido) {
            throw new UnsupportedOperationException("La consulta de compras no guarda pedidos");
        }

        @Override
        public boolean existePagadoConProducto(String clienteId, String productoId) {
            consultas.add(par(clienteId, productoId));
            return pagados.contains(par(clienteId, productoId));
        }

        @Override
        public List<Pedido> buscarPorCliente(String clienteId) {
            consultasPorCliente.add(clienteId);
            return delCliente.getOrDefault(clienteId, List.of());
        }

        @Override
        public Optional<Pedido> buscarPorId(String id) {
            return Optional.ofNullable(porId.get(id));
        }
    }
}
