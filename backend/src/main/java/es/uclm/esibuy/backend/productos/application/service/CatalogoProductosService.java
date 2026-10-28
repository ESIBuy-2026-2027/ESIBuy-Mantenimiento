package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Catálogo de productos del cliente (HU7): delega la búsqueda en el repositorio.
 */
public class CatalogoProductosService implements CatalogoProductosUseCase {

    private final ProductoRepositoryPort productoRepository;

    public CatalogoProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    @Override
    public ProductoPage buscarProductos(BuscarCatalogoQuery query) {
        return productoRepository.buscarEnCatalogo(query);
    }
}
