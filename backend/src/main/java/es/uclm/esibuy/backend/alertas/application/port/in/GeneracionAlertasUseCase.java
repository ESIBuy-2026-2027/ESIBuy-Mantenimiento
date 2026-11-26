package es.uclm.esibuy.backend.alertas.application.port.in;

/**
 * Generación de las alertas de los productos favoritos (AB#529). La usa el módulo de productos cada vez que cambia un
 * producto: lo hace un vendedor al modificarlo o activarlo, o un cliente al cancelar un pedido, que repone su stock.
 */
public interface GeneracionAlertasUseCase {

    /**
     * Crea una alerta para cada cliente que tiene el producto en favoritos si el precio que paga ha bajado (estándar o
     * premium, según el cliente) o si el producto ha vuelto a poder comprarse. Si no hay novedades, no hace nada.
     */
    void notificarCambio(CambioProducto cambio);
}
