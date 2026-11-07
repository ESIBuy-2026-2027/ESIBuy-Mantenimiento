package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;

/** Lo que estadísticas necesita del módulo de pedidos: las ventas del vendedor, sin los pedidos cancelados. */
public interface VentasPort {

    List<VentaRegistrada> ventasDe(String vendedorId);
}
