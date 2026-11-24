package es.uclm.esibuy.backend.pedidos.domain.exception;

/**
 * Algún producto del pedido ya no tiene stock suficiente (409 INSUFFICIENT_STOCK). Dice cuál es, para que el cliente
 * sepa qué producto tiene que cambiar: es un producto de su propio carrito.
 */
public class StockInsuficientePedidoException extends RuntimeException {

    private final String productoId;
    private final String nombreProducto;

    public StockInsuficientePedidoException(String mensaje, String productoId, String nombreProducto) {
        super(mensaje);
        this.productoId = productoId;
        this.nombreProducto = nombreProducto;
    }

    public String getProductoId() {
        return productoId;
    }

    public String getNombreProducto() {
        return nombreProducto;
    }
}
