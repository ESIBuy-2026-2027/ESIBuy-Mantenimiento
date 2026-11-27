package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;

/** Adaptador de salida hacia el módulo de pedidos: traduce sus ventas al dominio de estadísticas. */
public class VentasAdapter implements VentasPort {

    private final VentasVendedorUseCase ventasVendedor;

    public VentasAdapter(VentasVendedorUseCase ventasVendedor) {
        this.ventasVendedor = ventasVendedor;
    }

    @Override
    public List<VentaRegistrada> ventasDe(String vendedorId) {
        return ventasVendedor.ventasDe(vendedorId).stream()
                .map(venta -> new VentaRegistrada(venta.pedidoId(), venta.productoId(), venta.nombreProducto(),
                        venta.unidades(), venta.importe(), venta.fechaPedido()))
                .toList();
    }

    @Override
    public List<VentaRegistrada> ventasDe(String vendedorId, Optional<Instant> desde, Instant hasta) {
        List<VentaProducto> ventas = desde
                .map(inicio -> ventasVendedor.ventasDe(vendedorId, inicio, hasta))
                .orElseGet(() -> ventasVendedor.ventasDe(vendedorId));
        return ventas.stream()
                .filter(venta -> desde.isEmpty()
                        ? venta.fechaPedido() == null || venta.fechaPedido().isBefore(hasta)
                        : venta.fechaPedido() != null && !venta.fechaPedido().isBefore(desde.orElseThrow())
                                && venta.fechaPedido().isBefore(hasta))
                .map(venta -> new VentaRegistrada(venta.pedidoId(), venta.productoId(), venta.nombreProducto(),
                        venta.unidades(), venta.importe(), venta.fechaPedido()))
                .toList();
    }
}
