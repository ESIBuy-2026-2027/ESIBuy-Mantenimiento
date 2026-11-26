package es.uclm.esibuy.backend.alertas.domain.exception;

/**
 * La alerta no existe o es de otro cliente (AB#531): una alerta ajena se trata como inexistente, para no revelar que
 * existe.
 */
public class AlertaNoEncontradaException extends RuntimeException {

    public AlertaNoEncontradaException(String mensaje) {
        super(mensaje);
    }
}
