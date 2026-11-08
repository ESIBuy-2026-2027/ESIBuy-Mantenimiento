package es.uclm.esibuy.backend.usuarios.domain.exception;

/** El cliente ya es Premium (HU45, T422): no se puede volver a pasar a Premium. */
public class ClienteYaPremiumException extends RuntimeException {

    public ClienteYaPremiumException(String mensaje) {
        super(mensaje);
    }
}