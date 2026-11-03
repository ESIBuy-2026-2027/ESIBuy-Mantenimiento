package es.uclm.esibuy.backend.productos.application.service;

import java.util.List;

import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class GestionProductosVendedorService implements GestionProductosVendedorUseCase {

    private final ProductoRepositoryPort productoRepository;

    public GestionProductosVendedorService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public List<String> desactivarTodos(String vendedorId) {
        List<Producto> productos = productoRepository.listarTodosPorVendedor(vendedorId);
        productos.stream()
                .filter(Producto::isDisponible)
                .map(producto -> producto.conDisponibilidad(false))
                .forEach(productoRepository::guardar);
        return productos.stream().map(Producto::getId).toList();
    }

    @PreAuthorize(Permisos.SOLO_VENDEDOR)
    @Override
    public void eliminar(String productoId) {
        productoRepository.eliminar(productoId);
    }
}
