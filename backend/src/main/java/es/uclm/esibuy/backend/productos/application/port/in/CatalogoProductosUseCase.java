package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.Optional;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Puerto de entrada del catálogo de productos del cliente (HU7, HU8 y HU9).
 */
public interface CatalogoProductosUseCase {

    /**
     * Devuelve la página del catálogo con los productos disponibles que cumplen los criterios.
     *
     * @param query criterios de búsqueda, filtro, orden y paginación
     * @return resultado paginado de productos
     */
    ProductoPage buscarProductos(BuscarCatalogoQuery query);

    /**
     * Devuelve lo comercial de un producto si se puede comprar (existe y está disponible), con el precio final
     * para el cliente (HU9).
     *
     * @param clientePremium si el cliente es premium, para aplicarle su descuento
     * @return vacío si el producto no existe o no está disponible
     */
    Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium);

    /**
     * Devuelve el resumen de un producto exista o no en el catálogo: a diferencia de {@link #consultarComprable},
     * también el de uno que el vendedor ha desactivado (HU14).
     *
     * @param clientePremium si el cliente es premium, para aplicarle su descuento
     * @return vacío solo si el producto no existe
     */
    Optional<ResumenProducto> consultarResumen(String productoId, boolean clientePremium);

    /**
     * Devuelve el detalle de un producto del catálogo (HU8), con el nombre comercial de su vendedor y el precio
     * con el descuento que le corresponde al cliente.
     *
     * @param emailCliente email del cliente en sesión, para saber si es premium y aplicarle su descuento
     * @return el detalle del producto
     * @throws es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException si no existe o no
     *         está disponible
     */
    DetalleProductoCatalogo consultarDetalle(String productoId, String emailCliente);
}
