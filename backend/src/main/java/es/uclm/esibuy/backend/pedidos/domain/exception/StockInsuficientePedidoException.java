package es.uclm.esibuy.backend.pedidos.domain.exception;

/** Algún producto del pedido ya no tiene stock suficiente (409 INSUFFICIENT_STOCK). */
public class StockInsuficientePedidoException extends RuntimeException {

    public StockInsuficientePedidoException(String mensaje) {
        super(mensaje);
    }
}
