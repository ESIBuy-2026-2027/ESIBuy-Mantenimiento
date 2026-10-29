package es.uclm.esibuy.backend.pedidos.application.port.in;

import es.uclm.esibuy.backend.pedidos.domain.model.Pedido;

/**
 * Realización de pedidos por parte del cliente (HU10): un pedido por proveedor con lo que el cliente tiene de él
 * en el carrito. El cliente sale siempre de la sesión.
 */
public interface RealizacionPedidoUseCase {

    /**
     * Crea y guarda el pedido de un proveedor, descuenta el stock y quita esos productos del carrito. Si algo falla,
     * no se crea el pedido y el stock queda como estaba.
     *
     * @throws es.uclm.esibuy.backend.pedidos.domain.exception.MetodoPagoNoValidoException si el método no es suyo
     * @throws es.uclm.esibuy.backend.pedidos.domain.exception.DireccionNoValidaException si la dirección no es suya
     * @throws es.uclm.esibuy.backend.pedidos.domain.exception.CarritoSinProductosDelProveedorException si no tiene
     *         productos de ese proveedor en el carrito
     * @throws es.uclm.esibuy.backend.pedidos.domain.exception.StockInsuficienteException si falta stock
     */
    Pedido realizar(RealizarPedidoCommand comando);
}
