package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.pedidos;

import java.util.Set;

import es.uclm.esibuy.backend.pedidos.application.port.in.ProductosConPedidosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.PedidosVendedorPort;

/** Adaptador de salida hacia la consulta de pedidos del vendedor. */
public class PedidosVendedorAdapter implements PedidosVendedorPort {

    private final ProductosConPedidosUseCase pedidos;

    public PedidosVendedorAdapter(ProductosConPedidosUseCase pedidos) {
        this.pedidos = pedidos;
    }

    @Override
    public Set<String> buscarDeVendedor(String vendedorId) {
        return pedidos.buscarDeVendedor(vendedorId);
    }
}
