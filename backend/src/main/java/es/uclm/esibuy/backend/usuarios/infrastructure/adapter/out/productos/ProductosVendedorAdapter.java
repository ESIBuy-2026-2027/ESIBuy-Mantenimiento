package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.productos;

import java.util.List;

import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosVendedorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.ProductosVendedorPort;

/** Adaptador de salida hacia las operaciones de productos del vendedor. */
public class ProductosVendedorAdapter implements ProductosVendedorPort {

    private final GestionProductosVendedorUseCase productos;

    public ProductosVendedorAdapter(GestionProductosVendedorUseCase productos) {
        this.productos = productos;
    }

    @Override
    public List<String> desactivarTodos(String vendedorId) {
        return productos.desactivarTodos(vendedorId);
    }

    @Override
    public void eliminar(String productoId) {
        productos.eliminar(productoId);
    }
}
