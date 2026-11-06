package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/** Adaptador de salida que implementa el repositorio de incidencias con Spring Data MongoDB. */
public class IncidenciaMongoAdapter implements IncidenciaRepositoryPort {

    // De las más antiguas a las más recientes; con la misma fecha, por id, para que las páginas sean estables
    private static final Sort DE_MAS_ANTIGUA_A_MAS_RECIENTE = Sort.by(Sort.Direction.ASC, "fechaCreacion", "id");

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

    @Override
    public IncidenciaPage buscarPorAutor(String autorId, int pagina, int tamano) {
        Page<IncidenciaDocument> documentos = repository.findByUsuarioId(autorId,
                PageRequest.of(pagina, tamano, DE_MAS_ANTIGUA_A_MAS_RECIENTE));
        List<Incidencia> incidencias = documentos.getContent().stream().map(mapper::aDominio).toList();
        return new IncidenciaPage(incidencias, pagina, tamano, documentos.getTotalElements());
    }
}