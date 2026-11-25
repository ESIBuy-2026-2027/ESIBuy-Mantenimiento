package es.uclm.esibuy.backend.incidencias.domain.exception;

/**
 * Se intenta resolver o abrir una incidencia que ya está resuelta (HU192). Así la fecha de resolución original no se
 * sobrescribe y una incidencia resuelta no se reabre.
 */
public class IncidenciaYaResueltaException extends RuntimeException {

    public IncidenciaYaResueltaException() {
        super("La incidencia ya está resuelta");
    }
}
