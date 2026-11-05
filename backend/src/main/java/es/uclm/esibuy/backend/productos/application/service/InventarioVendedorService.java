package es.uclm.esibuy.backend.productos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoInventario;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Productos de un vendedor con su stock y sus valoraciones, para sus estadísticas (HU17). */
public class InventarioVendedorService implements InventarioVendedorUseCase {

    private final ProductoRepositoryPort productoRepository;

    public InventarioVendedorService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public List<ProductoInventario> productosDe(String vendedorId) {
        return productoRepository.buscarTodosPorVendedor(vendedorId).stream()
                .map(producto -> new ProductoInventario(producto.getId(), producto.getNombre(), producto.getStock(),
                        producto.isDisponible(), producto.getValoracionMedia(), producto.getNumeroValoraciones()))
                .toList();
    }
}
