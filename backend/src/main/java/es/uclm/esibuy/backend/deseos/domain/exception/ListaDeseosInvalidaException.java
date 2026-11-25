package es.uclm.esibuy.backend.deseos.domain.exception;

/** Se lanza cuando el nombre de una lista de deseos es vacío o supera la longitud máxima permitida. */
public class ListaDeseosInvalidaException extends RuntimeException {

    public ListaDeseosInvalidaException(String mensaje) {
        super(mensaje);
    }
}
