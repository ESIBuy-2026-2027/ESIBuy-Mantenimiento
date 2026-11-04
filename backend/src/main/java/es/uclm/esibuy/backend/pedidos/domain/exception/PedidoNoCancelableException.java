package es.uclm.esibuy.backend.pedidos.domain.exception;

// El pedido ya se ha enviado (o ya está cancelado) y no se puede cancelar (HU12-CP02)
public class PedidoNoCancelableException extends RuntimeException {

    public PedidoNoCancelableException(String mensaje) {
        super(mensaje);
    }
}
