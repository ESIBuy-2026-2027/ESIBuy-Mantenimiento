package es.uclm.esibuy.backend.pedidos.application.port.out;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Persistencia de los pedidos. */
public interface PedidoRepositoryPort {

    // Devuelve el pedido guardado, con el id que le ha asignado la base de datos
    Pedido guardar(Pedido pedido);

    // Si el cliente tiene algún pedido pagado que contenga el producto
    boolean existePagadoConProducto(String clienteId, String productoId);

    // Los pedidos del cliente, con el más reciente primero
    List<Pedido> buscarPorCliente(String clienteId);

    Optional<Pedido> buscarPorId(String id);
}
