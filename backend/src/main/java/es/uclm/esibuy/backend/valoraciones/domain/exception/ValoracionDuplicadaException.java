package es.uclm.esibuy.backend.valoraciones.domain.exception;

/** El cliente ya ha valorado este producto (HU13): solo hay una valoración por cliente y producto. */
public class ValoracionDuplicadaException extends RuntimeException {

    public ValoracionDuplicadaException() {
        super("Ya has valorado este producto");
    }
}
