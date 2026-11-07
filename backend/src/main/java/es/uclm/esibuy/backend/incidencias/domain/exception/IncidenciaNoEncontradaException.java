package es.uclm.esibuy.backend.incidencias.domain.exception;

/** La incidencia que se quiere abrir o cerrar no existe (HU50), por ejemplo porque otro administrador ya la cerró. */
public class IncidenciaNoEncontradaException extends RuntimeException {

    public IncidenciaNoEncontradaException() {
        super("La incidencia no existe");
    }
}
