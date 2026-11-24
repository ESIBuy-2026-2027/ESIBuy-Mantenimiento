package es.uclm.esibuy.backend.pedidos.domain.exception;

/** El pago del pedido ha sido rechazado: el pedido no se confirma ni se descuenta stock (402 PAYMENT_REJECTED). */
public class PagoRechazadoException extends RuntimeException {

    public PagoRechazadoException(String mensaje) {
        super(mensaje);
    }
}
