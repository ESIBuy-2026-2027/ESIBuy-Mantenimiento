package es.uclm.esibuy.backend.preferencias.domain.exception;

/** Las categorías elegidas no son válidas: faltan, o alguna no existe o no está activa. */
public class PreferenciasInvalidasException extends RuntimeException {

    public PreferenciasInvalidasException(String mensaje) {
        super(mensaje);
    }
}
