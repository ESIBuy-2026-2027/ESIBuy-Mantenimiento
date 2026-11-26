package es.uclm.esibuy.backend.alertas.domain.exception;

/** Se lanza cuando una alerta no cumple sus reglas: le falta un dato obligatorio o el precio no ha bajado. */
public class AlertaInvalidaException extends RuntimeException {

    public AlertaInvalidaException(String mensaje) {
        super(mensaje);
    }
}
