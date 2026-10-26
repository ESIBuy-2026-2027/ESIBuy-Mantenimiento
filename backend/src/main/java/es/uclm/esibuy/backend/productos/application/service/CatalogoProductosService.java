package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

public class CatalogoProductosService {

    private final ProductoRepositoryPort productoRepository;

    public CatalogoProductosService(ProductoRepositoryPort productoRepository) {
        this.productoRepository = productoRepository;
    }

    public ProductoPage buscarProductos(BuscarCatalogoQuery query) {
        return productoRepository.buscarEnCatalogo(query);
    }
    
}
