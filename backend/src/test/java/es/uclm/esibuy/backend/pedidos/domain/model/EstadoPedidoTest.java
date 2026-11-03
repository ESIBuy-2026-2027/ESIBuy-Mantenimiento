package es.uclm.esibuy.backend.pedidos.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Estados de envío de un pedido (HU11, T378). El orden del enum es el de la máquina de estados que ve el cliente
// en el detalle del pedido; CANCELADO va al final porque no es un paso del envío (lo usará la cancelación, HU12).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU11-CP05, propuesto.
class EstadoPedidoTest {

    @Test
    @DisplayName("HU11-CP05: los estados de envío van en el orden en que avanza un pedido, con CANCELADO al final")
    void losEstadosVanEnElOrdenDelEnvio() {
        assertEquals(List.of(EstadoPedido.PAGADO, EstadoPedido.PREPARANDO, EstadoPedido.ENVIADO,
                EstadoPedido.ENTREGADO, EstadoPedido.CANCELADO), List.of(EstadoPedido.values()));
    }
}
