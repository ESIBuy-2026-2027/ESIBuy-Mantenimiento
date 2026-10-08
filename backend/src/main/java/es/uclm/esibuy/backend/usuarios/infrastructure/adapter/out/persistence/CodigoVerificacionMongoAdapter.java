package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoVerificacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;

import static es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionDocumentMapper.PROPOSITO_LOGIN;

/**
 * Adaptador de salida que guarda en MongoDB los códigos de verificación del login (patrón Repository, T232).
 *
 * <p>Implementa el puerto que necesita el núcleo ({@link CodigoVerificacionRepositoryPort}) usando el
 * repositorio de Spring Data. Así, dominio y aplicación no dependen de MongoDB.
 */
public class CodigoVerificacionMongoAdapter implements CodigoVerificacionRepositoryPort {

    private final CodigoVerificacionMongoRepository repository;
    private final CodigoVerificacionDocumentMapper mapper;

    public CodigoVerificacionMongoAdapter(CodigoVerificacionMongoRepository repository,
            CodigoVerificacionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(CodigoVerificacion codigo) {
        if (codigo.getId() == null) {
            // Código nuevo (un login nuevo): sustituye al anterior del usuario, que ya no debe valer (HU2-CP11).
            repository.deleteByUsuarioIdAndPurpose(codigo.getUsuarioId(), PROPOSITO_LOGIN);
        }
        repository.save(mapper.aDocumento(codigo));
    }

    @Override
    public Optional<CodigoVerificacion> buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioIdAndPurpose(usuarioId, PROPOSITO_LOGIN).map(mapper::aDominio);
    }
}
