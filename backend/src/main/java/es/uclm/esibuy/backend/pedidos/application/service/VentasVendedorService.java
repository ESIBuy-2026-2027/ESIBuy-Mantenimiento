package es.uclm.esibuy.backend.pedidos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;
import es.uclm.esibuy.backend.pedidos.application.port.out.PedidoRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Ventas de un vendedor (HU17): cuenta como venta cada línea de un pedido suyo que no se ha cancelado. */
public class VentasVendedorService implements VentasVendedorUseCase {

    private final PedidoRepositoryPort pedidoRepository;

    public VentasVendedorService(PedidoRepositoryPort pedidoRepository) {
        this.pedidoRepository = pedidoRepository;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public List<VentaProducto> ventasDe(String vendedorId) {
        throw new UnsupportedOperationException("Funcionalidad no implementada");
    }
}
