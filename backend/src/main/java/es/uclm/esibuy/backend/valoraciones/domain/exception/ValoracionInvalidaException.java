package es.uclm.esibuy.backend.valoraciones.domain.exception;

/** Una valoración que no cumple las reglas del dominio (HU13), por ejemplo una puntuación fuera de 1 a 5. */
public class ValoracionInvalidaException extends RuntimeException {

    public ValoracionInvalidaException(String mensaje) {
        super(mensaje);
    }
}