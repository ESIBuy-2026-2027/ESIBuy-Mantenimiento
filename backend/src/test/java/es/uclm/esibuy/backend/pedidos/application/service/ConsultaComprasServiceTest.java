package es.uclm.esibuy.backend.pedidos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import es.uclm.esibuy.backend.pedidos.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Consulta de las compras de un cliente (HU13, T370): valoraciones pregunta a pedidos si el cliente ha comprado
// un producto antes de dejarle valorarlo. Sin Spring: los puertos de salida se sustituyen por dobles de prueba
// (clases internas de este test).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP06, propuesto.
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

    // Pedidos pagados guardados como pares cliente|producto; apunta cada consulta que recibe
    private static final class PedidosPagadosFijos implements PedidoRepositoryPort {
        private final Set<String> pagados = new HashSet<>();
        private final List<String> consultas = new ArrayList<>();

        @Override
        public Pedido guardar(Pedido pedido) {
            throw new UnsupportedOperationException("La consulta de compras no guarda pedidos");
        }

        @Override
        public boolean existePagadoConProducto(String clienteId, String productoId) {
            consultas.add(par(clienteId, productoId));
            return pagados.contains(par(clienteId, productoId));
        }
    }
}
