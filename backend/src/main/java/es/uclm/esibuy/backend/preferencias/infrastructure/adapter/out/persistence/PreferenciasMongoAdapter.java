package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.preferencias.application.port.out.PreferenciasRepositoryPort;
import es.uclm.esibuy.backend.preferencias.domain.model.Preferencias;

/** Adaptador de salida que implementa el repositorio de preferencias con Spring Data MongoDB. */
public class PreferenciasMongoAdapter implements PreferenciasRepositoryPort {

    private final PreferenciasMongoRepository repository;
    private final PreferenciasDocumentMapper mapper;

    public PreferenciasMongoAdapter(PreferenciasMongoRepository repository, PreferenciasDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public Optional<Preferencias> buscarPorCliente(String clienteId) {
        return repository.findByClienteId(clienteId).map(mapper::aDominio);
    }

    @Override
    public Preferencias guardar(Preferencias preferencias) {
        PreferenciasDocument documento = mapper.aDocumento(preferencias);
        if (preferencias.getId() != null) {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(preferencias.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        return mapper.aDominio(repository.save(documento));
    }
}
