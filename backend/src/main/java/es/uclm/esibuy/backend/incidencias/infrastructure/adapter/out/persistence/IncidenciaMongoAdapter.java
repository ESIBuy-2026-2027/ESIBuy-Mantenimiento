package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/** Adaptador de salida que implementa el repositorio de incidencias con Spring Data MongoDB. */
public class IncidenciaMongoAdapter implements IncidenciaRepositoryPort {

    private final IncidenciaMongoRepository repository;
    private final IncidenciaDocumentMapper mapper;

    public IncidenciaMongoAdapter(IncidenciaMongoRepository repository, IncidenciaDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Incidencia guardar(Incidencia incidencia) {
        return mapper.aDominio(repository.save(mapper.aDocumento(incidencia)));
    }
}