package es.uclm.esibuy.backend.incidencias.domain.exception;

/**
 * Se intenta abrir una incidencia que ya está abierta (HU50). Solo se abre una incidencia pendiente: así un
 * administrador no sobrescribe sin querer la respuesta que ya le dio otro.
 */
public class IncidenciaYaAbiertaException extends RuntimeException {

    public IncidenciaYaAbiertaException() {
        super("La incidencia ya está abierta");
    }
}
