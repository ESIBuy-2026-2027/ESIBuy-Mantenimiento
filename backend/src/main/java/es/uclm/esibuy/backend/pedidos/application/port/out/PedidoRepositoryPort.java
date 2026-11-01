package es.uclm.esibuy.backend.pedidos.application.port.out;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Persistencia de los pedidos. */
public interface PedidoRepositoryPort {

    // Devuelve el pedido guardado, con el id que le ha asignado la base de datos
    Pedido guardar(Pedido pedido);

    // Si el cliente tiene algún pedido pagado que contenga el producto
    boolean existePagadoConProducto(String clienteId, String productoId);
}
