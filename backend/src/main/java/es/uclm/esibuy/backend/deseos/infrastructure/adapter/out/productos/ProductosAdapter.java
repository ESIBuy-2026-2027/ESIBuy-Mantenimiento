package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.productos;

import java.util.Optional;

import es.uclm.esibuy.backend.deseos.application.port.out.ProductoDeseado;
import es.uclm.esibuy.backend.deseos.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;

/** Adaptador de salida que pregunta al módulo de productos por su puerto de entrada, sin usar su dominio. */
public class ProductosAdapter implements ProductosPort {

    private final CatalogoProductosUseCase catalogo;

    public ProductosAdapter(CatalogoProductosUseCase catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public boolean estaDisponible(String productoId) {
        // Solo importa si se puede añadir: aquí no se usa el precio, así que da igual si el cliente es premium
        return catalogo.consultarComprable(productoId, false).isPresent();
    }

    @Override
    public Optional<ProductoDeseado> consultarResumen(String productoId, boolean clientePremium) {
        return catalogo.consultarResumen(productoId, clientePremium)
                .map(producto -> new ProductoDeseado(producto.id(), producto.nombre(), producto.imagen(),
                        producto.precioFinal(), producto.stock(), producto.disponible()));
    }
}
