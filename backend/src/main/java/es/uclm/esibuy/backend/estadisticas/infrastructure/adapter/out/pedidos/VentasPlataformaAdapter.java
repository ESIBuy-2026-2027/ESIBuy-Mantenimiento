package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.application.port.out.VentasPlataformaPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasPlataformaUseCase;

/** Adaptador de salida hacia pedidos para consultar las ventas de toda la plataforma. */
public class VentasPlataformaAdapter implements VentasPlataformaPort {

    private final VentasPlataformaUseCase ventasPlataforma;

    public VentasPlataformaAdapter(VentasPlataformaUseCase ventasPlataforma) {
        this.ventasPlataforma = ventasPlataforma;
    }

    @Override
    public List<VentaRegistrada> consultar() {
        return ventasPlataforma.consultar().stream()
                .map(venta -> new VentaRegistrada(venta.pedidoId(), venta.productoId(), venta.nombreProducto(),
                        venta.unidades(), venta.importe()))
                .toList();
    }
}
