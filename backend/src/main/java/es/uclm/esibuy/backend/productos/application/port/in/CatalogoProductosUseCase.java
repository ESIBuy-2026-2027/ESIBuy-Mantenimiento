package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.Optional;

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

    /**
     * Devuelve lo comercial de un producto si se puede comprar (existe y está disponible), con el precio final
     * para el cliente (HU9).
     *
     * @param clientePremium si el cliente es premium, para aplicarle su descuento
     * @return vacío si el producto no existe o no está disponible
     */
    Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium);

    /**
     * Devuelve el detalle de un producto del catálogo (HU8), con el nombre comercial de su vendedor y el precio
     * con el descuento que le corresponde al cliente.
     * 
     * @param emailCliente email del cliente, para saber si es premium y aplicarle su descuento
     * @return detalle del producto, o vacío si no existe o no está disponible
     */
    DetalleProductoCatalogo consultarDetalle(String productoId, String emailCliente);
}
