package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.util.List;
import java.time.Instant;
import java.util.Optional;

import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;

/** Lo que estadísticas necesita del módulo de pedidos: las ventas del vendedor, sin los pedidos cancelados. */
public interface VentasPort {

    List<VentaRegistrada> ventasDe(String vendedorId);

    default List<VentaRegistrada> ventasDe(String vendedorId, Optional<Instant> desde, Instant hasta) {
        return ventasDe(vendedorId).stream()
                .filter(venta -> desde
                        .map(inicio -> venta.fechaPedido() != null
                                && !venta.fechaPedido().isBefore(inicio)
                                && venta.fechaPedido().isBefore(hasta))
                        .orElse(venta.fechaPedido() == null || venta.fechaPedido().isBefore(hasta)))
                .toList();
    }
}
