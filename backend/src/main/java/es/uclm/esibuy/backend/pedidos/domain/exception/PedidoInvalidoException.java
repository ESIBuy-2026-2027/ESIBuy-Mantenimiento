package es.uclm.esibuy.backend.pedidos.domain.exception;

/** Un pedido o una línea que no cumple las reglas del dominio (400). */
public class PedidoInvalidoException extends RuntimeException {

    public PedidoInvalidoException(String mensaje) {
        super(mensaje);
    }
}
