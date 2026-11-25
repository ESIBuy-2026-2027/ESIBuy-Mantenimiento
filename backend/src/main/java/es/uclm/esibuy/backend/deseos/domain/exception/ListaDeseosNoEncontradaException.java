package es.uclm.esibuy.backend.deseos.domain.exception;

/** Se lanza cuando la lista de deseos solicitada no existe o no pertenece al cliente en sesión. */
public class ListaDeseosNoEncontradaException extends RuntimeException {

    public ListaDeseosNoEncontradaException() {
        super("La lista de deseos no existe");
    }
}
