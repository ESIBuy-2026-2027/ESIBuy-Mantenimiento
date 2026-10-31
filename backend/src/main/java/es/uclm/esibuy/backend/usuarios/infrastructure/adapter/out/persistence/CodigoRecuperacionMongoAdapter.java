package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.application.port.out.CodigoRecuperacionRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.CodigoVerificacion;
import static es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence.CodigoVerificacionDocumentMapper.PROPOSITO_RECUPERACION;

/**
 * Adaptador de salida que guarda en MongoDB los códigos de recuperación de contraseña (HU3).
 *
 * <p>Comparte colección ({@code tokens_seguridad}) y repositorio con los códigos del login, pero todo
 * filtra por el propósito {@code PASSWORD_RECOVERY}: un código de recuperación nunca borra ni devuelve
 * el del login, ni al revés.
 */
public class CodigoRecuperacionMongoAdapter implements CodigoRecuperacionRepositoryPort {

    private final CodigoVerificacionMongoRepository repository;
    private final CodigoVerificacionDocumentMapper mapper;

    public CodigoRecuperacionMongoAdapter(CodigoVerificacionMongoRepository repository,
            CodigoVerificacionDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(CodigoVerificacion codigo) {
        CodigoVerificacionDocument documento = mapper.aDocumento(codigo, PROPOSITO_RECUPERACION);
        if (codigo.getId() == null) {
            // Código nuevo (una solicitud nueva): sustituye al de recuperación anterior del usuario, que ya no debe valer.
            repository.deleteByUsuarioIdAndPurpose(codigo.getUsuarioId(), PROPOSITO_RECUPERACION);
        } else {
            // Actualización (un fallo o el uso del código): se conserva la fecha de creación del documento guardado.
            repository.findById(codigo.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        repository.save(documento);
    }

    @Override
    public Optional<CodigoVerificacion> buscarPorUsuario(String usuarioId) {
        return repository.findByUsuarioIdAndPurpose(usuarioId, PROPOSITO_RECUPERACION).map(mapper::aDominio);
    }
}
