package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import es.uclm.esibuy.backend.incidencias.application.port.out.IncidenciaRepositoryPort;
import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/** Adaptador de salida que implementa el repositorio de incidencias con Spring Data MongoDB. */
public class IncidenciaMongoAdapter implements IncidenciaRepositoryPort {

    // De las más antiguas a las más recientes; con la misma fecha, por id, para que las páginas sean estables
    private static final Sort DE_MAS_ANTIGUA_A_MAS_RECIENTE = Sort.by(
            Sort.Order.asc("fechaCreacion"),
            Sort.Order.asc("id"));

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
        return aPagina(documentos, pagina, tamano);
    }

    // findAll con paginación ya viene en MongoRepository: no hace falta tocar IncidenciaMongoRepository
    @Override
    public IncidenciaPage buscarTodas(int pagina, int tamano) {
        Page<IncidenciaDocument> documentos = repository.findAll(
                PageRequest.of(pagina, tamano, DE_MAS_ANTIGUA_A_MAS_RECIENTE));
        return aPagina(documentos, pagina, tamano);
    }

    @Override
    public IncidenciaPage buscarPorEstado(EstadoIncidencia estado, int pagina, int tamano) {
        Page<IncidenciaDocument> documentos = porEstado(estado,
                PageRequest.of(pagina, tamano, DE_MAS_ANTIGUA_A_MAS_RECIENTE));
        return aPagina(documentos, pagina, tamano);
    }

    @Override
    public IncidenciaPage buscarSupervisadas(EstadoIncidencia estado, String busqueda, String ordenarPor, String sentido,
            int pagina, int tamano) {
        Sort.Direction direccion = "desc".equalsIgnoreCase(sentido) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String campo = (ordenarPor == null || ordenarPor.isBlank()) ? "fechaCreacion" : ordenarPor;
        PageRequest pageable = PageRequest.of(pagina, tamano, orden(direccion, campo));
        String texto = busqueda == null ? "" : busqueda.trim();

        Page<IncidenciaDocument> documentos;
        if (estado != null && !texto.isEmpty()) {
            documentos = porEstadoYAsunto(estado, texto, pageable);
        } else if (estado != null) {
            documentos = porEstado(estado, pageable);
        } else if (!texto.isEmpty()) {
            documentos = repository.findByAsuntoContainingIgnoreCase(texto, pageable);
        } else {
            documentos = repository.findAll(pageable);
        }
        return aPagina(documentos, pagina, tamano);
    }

    @Override
    public boolean registrarResolucion(Incidencia resuelta) {
        return repository.registrarResolucion(resuelta.id(), resuelta.fechaResolucion()) > 0;
    }

    @Override
    public Optional<Incidencia> buscarPorId(String id) {
        return repository.findById(id).map(mapper::aDominio);
    }

    @Override
    public void eliminar(String id) {
        repository.deleteById(id);
    }

    // Al ordenar por estado, la fecha de resolución separa las abiertas de las resueltas, que comparten estado guardado
    private static Sort orden(Sort.Direction direccion, String campo) {
        return "estado".equals(campo)
                ? Sort.by(direccion, campo, "fechaResolucion", "id")
                : Sort.by(direccion, campo, "id");
    }

    private Page<IncidenciaDocument> porEstado(EstadoIncidencia estado, Pageable pageable) {
        return estado == EstadoIncidencia.RESUELTA
                ? repository.findByFechaResolucionNotNull(pageable)
                : repository.findByEstadoAndFechaResolucionIsNull(estado.name(), pageable);
    }

    private Page<IncidenciaDocument> porEstadoYAsunto(EstadoIncidencia estado, String asunto, Pageable pageable) {
        return estado == EstadoIncidencia.RESUELTA
                ? repository.findByFechaResolucionNotNullAndAsuntoContainingIgnoreCase(asunto, pageable)
                : repository.findByEstadoAndFechaResolucionIsNullAndAsuntoContainingIgnoreCase(estado.name(), asunto,
                        pageable);
    }

    // Común a las dos búsquedas: convierte la página de documentos en la página del dominio
    private IncidenciaPage aPagina(Page<IncidenciaDocument> documentos, int pagina, int tamano) {
        List<Incidencia> incidencias = documentos.getContent().stream().map(mapper::aDominio).toList();
        return new IncidenciaPage(incidencias, pagina, tamano, documentos.getTotalElements());
    }
}
