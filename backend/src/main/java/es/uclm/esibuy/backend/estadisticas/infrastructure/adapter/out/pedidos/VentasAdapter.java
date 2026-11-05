package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;

/** Adaptador de salida hacia el módulo de pedidos: traduce sus ventas al dominio de estadísticas. */
public class VentasAdapter implements VentasPort {

    private final VentasVendedorUseCase ventasVendedor;

    public VentasAdapter(VentasVendedorUseCase ventasVendedor) {
        this.ventasVendedor = ventasVendedor;
    }

    @Override
    public List<VentaRegistrada> ventasDe(String vendedorId) {
        throw new UnsupportedOperationException("Funcionalidad no implementada");
    }
}
