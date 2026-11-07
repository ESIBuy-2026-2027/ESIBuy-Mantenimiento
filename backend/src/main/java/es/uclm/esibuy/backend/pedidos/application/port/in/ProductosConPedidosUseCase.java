package es.uclm.esibuy.backend.pedidos.application.port.in;

import java.util.Set;

/**
 * Consulta interna de los productos que deben conservarse por formar parte de pedidos.
 */
public interface ProductosConPedidosUseCase {

    Set<String> buscarDeVendedor(String vendedorId);
}
