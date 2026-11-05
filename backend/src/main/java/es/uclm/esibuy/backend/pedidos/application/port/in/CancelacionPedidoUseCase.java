package es.uclm.esibuy.backend.pedidos.application.port.in;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Cancelación de un pedido por el cliente (HU12). El cliente sale siempre de la sesión. */
public interface CancelacionPedidoUseCase {

    /**
     * Cancela un pedido del cliente y repone el stock de sus productos. Devuelve el pedido cancelado.
     */
    Pedido cancelarPedido(String emailCliente, String pedidoId);
}
