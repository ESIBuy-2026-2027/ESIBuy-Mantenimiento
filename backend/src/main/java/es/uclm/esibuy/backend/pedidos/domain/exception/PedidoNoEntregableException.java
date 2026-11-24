package es.uclm.esibuy.backend.pedidos.domain.exception;

// El pedido no está enviado (aún no sale, ya se entregó o se canceló) y no se puede marcar como entregado
public class PedidoNoEntregableException extends RuntimeException {

    public PedidoNoEntregableException(String mensaje) {
        super(mensaje);
    }
}
