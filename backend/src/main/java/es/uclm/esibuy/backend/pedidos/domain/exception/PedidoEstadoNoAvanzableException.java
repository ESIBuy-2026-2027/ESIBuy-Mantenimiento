package es.uclm.esibuy.backend.pedidos.domain.exception;

// El pedido ya está entregado o cancelado, o su estado no admite avanzar.
public class PedidoEstadoNoAvanzableException extends RuntimeException {

    public PedidoEstadoNoAvanzableException(String mensaje) {
        super(mensaje);
    }
}
