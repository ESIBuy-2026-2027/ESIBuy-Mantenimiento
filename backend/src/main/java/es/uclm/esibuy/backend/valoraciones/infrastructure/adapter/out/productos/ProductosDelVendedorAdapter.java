package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.productos;

import java.util.List;

import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.valoraciones.application.port.out.ProductosDelVendedorPort;

public class ProductosDelVendedorAdapter implements ProductosDelVendedorPort {

    private final InventarioVendedorUseCase inventarioVendedor;

    public ProductosDelVendedorAdapter(InventarioVendedorUseCase inventarioVendedor) {
        this.inventarioVendedor = inventarioVendedor;
    }

    @Override
    public List<String> idsDe(String vendedorId) {
        return inventarioVendedor.productosDe(vendedorId).stream()
                .map(producto -> producto.id())
                .toList();
    }
}
