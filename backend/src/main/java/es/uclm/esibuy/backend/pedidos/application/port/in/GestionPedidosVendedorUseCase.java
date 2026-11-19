package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

public interface GestionPedidosVendedorUseCase {

    List<Pedido> listarPedidos(String emailVendedor);

    Pedido consultarPedido(String emailVendedor, String pedidoId);

    Pedido avanzarEstado(String emailVendedor, String pedidoId);
}
