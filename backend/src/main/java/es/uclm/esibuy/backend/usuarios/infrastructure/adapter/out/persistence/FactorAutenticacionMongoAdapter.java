package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.application.port.out.FactorAutenticacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.FactorAutenticacion;

import java.util.Optional;

public class FactorAutenticacionMongoAdapter implements FactorAutenticacionRepositoryPort {

    private final FactorAutenticacionMongoRepository repository;
    private final FactorAutenticacionDocumentMapper mapper;

    public FactorAutenticacionMongoAdapter(FactorAutenticacionMongoRepository repository,
            FactorAutenticacionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(FactorAutenticacion factor) {
        FactorAutenticacionDocument documento = mapper.aDocumento(factor);
        if (factor.getId() == null) {
            // Alta nueva: sustituye al factor anterior del usuario (el índice único de usuarioId no deja dos)
            repository.deleteByUsuarioId(factor.getUsuarioId());
        } else {
            // Actualización: se conserva la fecha de creación del documento guardado
            repository.findById(factor.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        repository.save(documento);
    }

    @Override
    public Optional<FactorAutenticacion> buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioId(usuarioId).map(mapper::aDominio);
    }

    @Override
    public void eliminarPorUsuario(String usuarioId) {
        repository.deleteByUsuarioId(usuarioId);
    }
}