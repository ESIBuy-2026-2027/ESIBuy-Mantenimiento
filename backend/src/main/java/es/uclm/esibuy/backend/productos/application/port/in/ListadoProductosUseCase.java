package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Puerto de entrada del listado de productos del vendedor (HU32).
 */
public interface ListadoProductosUseCase {

    /**
     * Devuelve la página de productos del vendedor con sesión que cumple los criterios.
     *
     * @param emailVendedor correo del vendedor en sesión: solo se listan sus productos
     * @param query criterios de búsqueda, filtro y paginación
     * @return resultado paginado de productos
     */
    ProductoPage listarMisProductos(String emailVendedor, ListarProductosQuery query);
}
