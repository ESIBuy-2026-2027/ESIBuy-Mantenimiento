package es.uclm.esibuy.backend.productos.application.port.in;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Puerto de entrada del administrador para supervisar, en solo lectura, los productos de un vendedor (HU26).
 */
public interface SupervisionProductosUseCase {

    /**
     * Devuelve una página de productos del vendedor, del más reciente al más antiguo.
     *
     * @param vendedorId id del vendedor; si no existe o no es vendedor, falla
     * @param pagina índice de página, empezando en cero
     * @param tamano número máximo de productos por página
     * @return resultado paginado de productos
     */
    ProductoPage listarProductosDeVendedor(String vendedorId, int pagina, int tamano);
}
