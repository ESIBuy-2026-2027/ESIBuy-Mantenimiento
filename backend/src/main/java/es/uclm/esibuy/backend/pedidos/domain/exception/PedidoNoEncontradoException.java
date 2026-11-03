package es.uclm.esibuy.backend.pedidos.domain.exception;

// El pedido no existe o no es del cliente: las dos cosas dan la misma respuesta (HU11-CP03)
public class PedidoNoEncontradoException extends RuntimeException {

    public PedidoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
