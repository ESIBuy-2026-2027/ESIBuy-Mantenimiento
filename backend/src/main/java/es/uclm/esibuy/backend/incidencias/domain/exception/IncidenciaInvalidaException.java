package es.uclm.esibuy.backend.incidencias.domain.exception;

/** Una incidencia que no cumple las reglas del dominio (HU44), por ejemplo un asunto vacío o demasiado largo. */
public class IncidenciaInvalidaException extends RuntimeException {

    public IncidenciaInvalidaException(String mensaje) {
        super(mensaje);
    }
}