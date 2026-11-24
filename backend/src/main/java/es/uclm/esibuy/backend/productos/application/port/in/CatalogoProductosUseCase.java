package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.List;
import java.util.Optional;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Puerto de entrada del catálogo de productos del cliente (HU7, HU8 y HU9).
 */
public interface CatalogoProductosUseCase {

    /**
     * Devuelve los productos disponibles mejor valorados para la portada pública.
     * No incluye datos comerciales ni requiere una sesión de cliente.
     *
     * @param limite máximo de productos destacados
     * @return productos ordenados por valoración descendente
     */
    List<Producto> buscarDestacados(int limite);

    /**
     * Devuelve la página del catálogo con los productos disponibles que cumplen los
     * criterios, y si el cliente es Premium para darle a cada producto su precio.
     *
     * @param query criterios de búsqueda, filtro, orden y paginación
     * @param emailCliente email del cliente en sesión, para saber si es premium
     * @return la página de productos y si el cliente es premium
     */
    PaginaCatalogo buscarProductos(BuscarCatalogoQuery query, String emailCliente);

    Optional<ProductoComercial> consultarComprable(String productoId, boolean clientePremium);

    Optional<ResumenProducto> consultarResumen(String productoId, boolean clientePremium);

    DetalleProductoCatalogo consultarDetalle(String productoId, String emailCliente);
}
