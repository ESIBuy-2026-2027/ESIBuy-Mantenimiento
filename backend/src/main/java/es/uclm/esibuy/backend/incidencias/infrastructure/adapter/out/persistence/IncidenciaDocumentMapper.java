package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Esqueleto del ciclo rojo: todavía no convierte nada. */
public class IncidenciaDocumentMapper {

    public IncidenciaDocument aDocumento(Incidencia incidencia) {
        return new IncidenciaDocument();
    }

    public Incidencia aDominio(IncidenciaDocument documento) {
        return null;
    }
}