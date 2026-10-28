package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Puerto de entrada del catálogo de productos del cliente (HU7).
 */
public interface CatalogoProductosUseCase {

    /**
     * Devuelve la página del catálogo con los productos disponibles que cumplen los criterios.
     *
     * @param query criterios de búsqueda, filtro, orden y paginación
     * @return resultado paginado de productos
     */
    ProductoPage buscarProductos(BuscarCatalogoQuery query);
}
