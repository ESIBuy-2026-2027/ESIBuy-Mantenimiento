package es.uclm.esibuy.backend.preferencias.domain.exception;

/** La categoría no existe o no está activa, así que no se puede marcar como favorita (AB#528). */
public class CategoriaNoDisponibleException extends RuntimeException {

    public CategoriaNoDisponibleException(String mensaje) {
        super(mensaje);
    }
}
