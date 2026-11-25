package es.uclm.esibuy.backend.incidencias.domain.exception;

/** Se intenta resolver una incidencia que todavía no se ha abierto (HU192): antes la abre un administrador (HU50). */
public class IncidenciaNoAbiertaException extends RuntimeException {

    public IncidenciaNoAbiertaException() {
        super("Solo se puede resolver una incidencia abierta");
    }
}
