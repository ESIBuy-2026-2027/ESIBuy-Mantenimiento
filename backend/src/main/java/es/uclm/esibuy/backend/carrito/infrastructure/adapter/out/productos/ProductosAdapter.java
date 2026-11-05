package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.productos;

import java.util.Optional;

import es.uclm.esibuy.backend.carrito.application.port.out.ProductoCarrito;
import es.uclm.esibuy.backend.carrito.application.port.out.ProductosPort;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;

/**
 * Adaptador de salida que pregunta al módulo de productos por su puerto de entrada y traduce la respuesta a la
 * del carrito, sin usar su dominio.
 */
public class ProductosAdapter implements ProductosPort {

    private final CatalogoProductosUseCase catalogo;

    public ProductosAdapter(CatalogoProductosUseCase catalogo) {
        this.catalogo = catalogo;
    }

    @Override
    public Optional<ProductoCarrito> consultarComprable(String productoId, boolean clientePremium) {
        return catalogo.consultarComprable(productoId, clientePremium)
                .map(producto -> new ProductoCarrito(producto.id(), producto.vendedorId(), producto.nombre(),
                        producto.imagen(), producto.precioFinal(), producto.stock()));
    }
}
