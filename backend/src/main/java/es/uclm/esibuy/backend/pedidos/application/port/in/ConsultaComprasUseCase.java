package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Consulta de las compras del cliente: si ha comprado un producto, para poder valorarlo (HU13), y su historial
 * de pedidos con el detalle de cada uno (HU11). El cliente sale siempre de la sesión.
 */
public interface ConsultaComprasUseCase {

    /** Si el cliente tiene algún pedido pagado con ese producto. */
    boolean haComprado(String emailCliente, String productoId);

    /** El historial de pedidos del cliente, del más reciente al más antiguo. */
    List<Pedido> listarPedidos(String emailCliente);

    /** El detalle de un pedido del cliente; si no existe o es de otro cliente, PedidoNoEncontradoException. */
    Pedido consultarPedido(String emailCliente, String pedidoId);
}
