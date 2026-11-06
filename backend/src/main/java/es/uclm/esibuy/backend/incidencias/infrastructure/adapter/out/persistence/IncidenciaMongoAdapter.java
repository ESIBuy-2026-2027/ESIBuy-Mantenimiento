package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Esqueleto del ciclo rojo: todavía no guarda nada. */
public class IncidenciaMongoAdapter implements IncidenciaRepositoryPort {

    private final IncidenciaMongoRepository repository;
    private final IncidenciaDocumentMapper mapper;

    public IncidenciaMongoAdapter(IncidenciaMongoRepository repository, IncidenciaDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Incidencia guardar(Incidencia incidencia) {
        return incidencia;
    }
}