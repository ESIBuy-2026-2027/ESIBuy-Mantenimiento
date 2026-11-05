package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.application.port.out.InventarioPort;
import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoInventariado;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;

/** Adaptador de salida hacia el módulo de productos: traduce su inventario al dominio de estadísticas. */
public class InventarioAdapter implements InventarioPort {

    private final InventarioVendedorUseCase inventarioVendedor;

    public InventarioAdapter(InventarioVendedorUseCase inventarioVendedor) {
        this.inventarioVendedor = inventarioVendedor;
    }

    @Override
    public List<ProductoInventariado> productosDe(String vendedorId) {
        return inventarioVendedor.productosDe(vendedorId).stream()
                .map(producto -> new ProductoInventariado(producto.id(), producto.nombre(), producto.stock(),
                        producto.disponible(), producto.valoracionMedia(), producto.numeroValoraciones()))
                .toList();
    }
}
