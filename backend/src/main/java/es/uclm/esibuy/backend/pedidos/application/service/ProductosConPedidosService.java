package es.uclm.esibuy.backend.pedidos.application.service;

import java.util.HashSet;
import java.util.Set;

import es.uclm.esibuy.backend.pedidos.application.port.in.ProductosConPedidosUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class ProductosConPedidosService implements ProductosConPedidosUseCase {

    private final PedidoRepositoryPort pedidoRepository;

    public ProductosConPedidosService(PedidoRepositoryPort pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public Set<String> buscarDeVendedor(String vendedorId) {
        Set<String> productos = new HashSet<>();
        for (Pedido pedido : pedidoRepository.buscarPorVendedor(vendedorId)) {
            pedido.getLineas().forEach(linea -> productos.add(linea.productoId()));
        }
        return productos;
    }
}
