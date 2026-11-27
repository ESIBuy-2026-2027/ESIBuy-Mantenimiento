package es.uclm.esibuy.backend.productos.application.service;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.productos.application.port.in.ProductoEstadistica;
import es.uclm.esibuy.backend.productos.application.port.in.ProductosPlataformaUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/** Datos de valoración de todos los productos, para los rankings del administrador. */
public class ProductosPlataformaService implements ProductosPlataformaUseCase {

    private final ProductoRepositoryPort productos;

    public ProductosPlataformaService(ProductoRepositoryPort productos) {
        this.productos = productos;
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public List<ProductoEstadistica> listar() {
        return productos.listarTodos().stream()
                .map(producto -> new ProductoEstadistica(producto.getId(), producto.getNombre(),
                        producto.getVendedorId(), producto.getValoracionMedia(), producto.getNumeroValoraciones()))
                .toList();
    }
}
