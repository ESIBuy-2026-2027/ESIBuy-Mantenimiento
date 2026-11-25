package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/** Gestión de pedidos recibidos por el vendedor. El vendedor sale siempre de la sesión. */
public interface GestionPedidosVendedorUseCase {

    List<Pedido> listarPedidos(String emailVendedor);

    Pedido consultarPedido(String emailVendedor, String pedidoId);

    /** Avanza el estado del pedido hasta ENVIADO (PAGADO -> PREPARANDO -> ENVIADO). */
    Pedido avanzarEstado(String emailVendedor, String pedidoId);

    /** Retrocede el estado del pedido mientras no esté enviado (PREPARANDO -> PAGADO). */
    Pedido retrocederEstado(String emailVendedor, String pedidoId);

    /** Cancela un pedido del vendedor mientras no esté enviado y repone el stock. */
    Pedido cancelarPedido(String emailVendedor, String pedidoId);
}