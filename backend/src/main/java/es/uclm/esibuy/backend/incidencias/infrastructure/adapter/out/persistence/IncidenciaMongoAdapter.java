package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import java.util.List;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

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

    // Esqueleto del ciclo rojo: todavía no consulta nada
    @Override
    public IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano) {
        return new IncidenciaPage(List.of(), pagina, tamano, 0);
    }
}