package es.uclm.esibuy.backend.productos.application.port.out;

import es.uclm.esibuy.backend.productos.domain.model.Producto;

/**
 * Aviso a las alertas de los productos favoritos (AB#529) cuando cambia un producto: el módulo de alertas decide si
 * ha bajado el precio o vuelve a estar disponible, y a quién avisar.
 */
public interface AlertasFavoritosPort {

    /**
     * Se llama después de guardar el cambio. Un fallo al generar las alertas no deshace el cambio del producto.
     *
     * @param antes el producto como estaba
     * @param despues el producto como ha quedado
     */
    void notificarCambio(Producto antes, Producto despues);
}
