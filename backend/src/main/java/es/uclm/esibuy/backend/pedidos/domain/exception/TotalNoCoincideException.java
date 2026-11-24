package es.uclm.esibuy.backend.pedidos.domain.exception;

/** El total que el cliente vio al confirmar ya no es el real: cambió un precio, un descuento o una cantidad (409 TOTAL_MISMATCH). */
public class TotalNoCoincideException extends RuntimeException {

    public TotalNoCoincideException(String mensaje) {
        super(mensaje);
    }
}
