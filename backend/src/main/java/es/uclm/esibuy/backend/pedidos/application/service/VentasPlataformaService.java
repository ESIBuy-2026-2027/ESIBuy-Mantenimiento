package es.uclm.esibuy.backend.pedidos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasPlataformaUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.pedidos.domain.model.EstadoPedido;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Ventas globales no canceladas, para el ranking de productos del administrador. */
public class VentasPlataformaService implements VentasPlataformaUseCase {

    private final PedidoRepositoryPort pedidos;

    public VentasPlataformaService(PedidoRepositoryPort pedidos) {
        this.pedidos = pedidos;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public List<VentaProducto> consultar() {
        return pedidos.buscarTodos().stream()
                .filter(pedido -> pedido.getEstado() != EstadoPedido.CANCELADO)
                .flatMap(pedido -> pedido.getLineas().stream().map(linea -> new VentaProducto(pedido.getId(),
                        linea.productoId(), linea.nombreProducto(), linea.cantidad(), linea.subtotal())))
                .toList();
    }
}
