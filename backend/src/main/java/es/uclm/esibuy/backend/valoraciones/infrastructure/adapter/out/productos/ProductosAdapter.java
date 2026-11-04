package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosPort;

/** Adaptador de valoraciones hacia el módulo de productos (HU13): usa su puerto de entrada, nunca su dominio. */
public class ProductosAdapter implements ProductosPort {

    private final ValoracionesProductoUseCase valoracionesProducto;

    public ProductosAdapter(ValoracionesProductoUseCase valoracionesProducto) {
        this.valoracionesProducto = valoracionesProducto;
    }

    @Override
    public void actualizarValoracion(String productoId, double media, int numero) {
        valoracionesProducto.actualizarValoracion(productoId, media, numero);
    }
}
