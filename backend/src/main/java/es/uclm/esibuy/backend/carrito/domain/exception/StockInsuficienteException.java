package es.uclm.esibuy.backend.carrito.domain.exception;

/** El carrito pediría más unidades de las que hay en stock (409 INSUFFICIENT_STOCK). */
public class StockInsuficienteException extends RuntimeException {

    public StockInsuficienteException(String mensaje) {
        super(mensaje);
    }
}
