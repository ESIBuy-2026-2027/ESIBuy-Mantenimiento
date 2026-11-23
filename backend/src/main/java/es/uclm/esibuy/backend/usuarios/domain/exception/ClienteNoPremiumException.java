package es.uclm.esibuy.backend.usuarios.domain.exception;

/** El cliente no tiene una suscripción Premium que pueda cancelar (CLI-02). */
public class ClienteNoPremiumException extends RuntimeException {

    public ClienteNoPremiumException(String mensaje) {
        super(mensaje);
    }
}
