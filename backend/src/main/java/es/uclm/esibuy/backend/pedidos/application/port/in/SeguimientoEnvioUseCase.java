package es.uclm.esibuy.backend.pedidos.application.port.in;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Seguimiento del envío por el cliente: consultar el estado y el histórico de su pedido y confirmar que lo ha
 * recibido. El cliente sale siempre de la sesión.
 */
public interface SeguimientoEnvioUseCase {

    /** El pedido del cliente con su estado, histórico y fechas de entrega; si no existe o es ajeno, PedidoNoEncontradoException. */
    Pedido consultarSeguimiento(String emailCliente, String pedidoId);

    /** El cliente confirma que ha recibido el pedido, que pasa a ENTREGADO. Solo es válido desde ENVIADO. */
    Pedido confirmarEntrega(String emailCliente, String pedidoId);
}
