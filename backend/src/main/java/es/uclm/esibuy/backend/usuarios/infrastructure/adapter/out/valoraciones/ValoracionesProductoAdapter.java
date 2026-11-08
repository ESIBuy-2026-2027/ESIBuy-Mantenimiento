package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.valoraciones;

import es.uclm.esibuy.backend.usuarios.application.port.out.ValoracionesProductoPort;
import es.uclm.esibuy.backend.valoraciones.application.port.in.LimpiezaValoracionesUseCase;

/** Adaptador de salida hacia la limpieza de valoraciones de un producto. */
public class ValoracionesProductoAdapter implements ValoracionesProductoPort {

    private final LimpiezaValoracionesUseCase valoraciones;

    public ValoracionesProductoAdapter(LimpiezaValoracionesUseCase valoraciones) {
        this.valoraciones = valoraciones;
    }

    @Override
    public void eliminarDeProducto(String productoId) {
        valoraciones.eliminarDeProducto(productoId);
    }
}
