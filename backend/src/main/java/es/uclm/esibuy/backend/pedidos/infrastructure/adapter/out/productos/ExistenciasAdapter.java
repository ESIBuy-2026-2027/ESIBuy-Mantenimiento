package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.pedidos.application.port.out.ExistenciasPort;
import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;

/** Adaptador de salida que descuenta y repone el stock por el puerto de entrada del módulo de productos. */
public class ExistenciasAdapter implements ExistenciasPort {

    private final ExistenciasProductosUseCase existencias;

    public ExistenciasAdapter(ExistenciasProductosUseCase existencias) {
        this.existencias = existencias;
    }

    @Override
    public boolean descontar(String productoId, int cantidad) {
        return existencias.descontar(productoId, cantidad);
    }

    @Override
    public void reponer(String productoId, int cantidad) {
        existencias.reponer(productoId, cantidad);
    }
}
