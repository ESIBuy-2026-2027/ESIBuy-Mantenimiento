package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import java.util.Optional;

import org.springframework.security.access.prepost.PreAuthorize;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

/**
 * Catálogo de productos del cliente (HU7): delega la búsqueda en el repositorio. También da a otros módulos lo
 * comercial de un producto que se puede comprar (HU9).
 */
public class CatalogoProductosService implements CatalogoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;

    public CatalogoProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public ProductoPage buscarProductos(BuscarCatalogoQuery query) {
        return productoRepository.buscarEnCatalogo(query);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE)
    @Override
    public Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium) {
        return productoRepository.buscarPorId(productoId)
                .filter(producto -> producto.isDisponible())
                .map(producto -> new ProductoComercial(producto.getId(), producto.getVendedorId(),
                        producto.getNombre(), producto.getImagen(), producto.precioFinal(clientePremium),
                        producto.getStock()));
    }
}
