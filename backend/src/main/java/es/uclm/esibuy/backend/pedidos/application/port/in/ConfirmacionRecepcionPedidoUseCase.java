package es.uclm.esibuy.backend.pedidos.application.port.in;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Confirmación de recepción de un pedido enviado por el cliente. El cliente sale siempre de la sesión. */
public interface ConfirmacionRecepcionPedidoUseCase {

    /**
     * Marca como entregado un pedido del cliente que se encuentra enviado. Devuelve el pedido actualizado.
     */
    Pedido confirmarRecepcion(String emailCliente, String pedidoId);
}