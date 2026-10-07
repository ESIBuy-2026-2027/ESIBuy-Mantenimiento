package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

import java.util.Optional;

/**
 * Adaptador de salida que guarda usuarios en MongoDB (patrón Repository).
 *
 * <p>Implementa el puerto que necesita el núcleo ({@link UsuarioRepositoryPort}) usando el
 * repositorio de Spring Data. Así, dominio y aplicación no dependen de MongoDB.
 */
public class UsuarioMongoAdapter implements UsuarioRepositoryPort {

    private final UsuarioMongoRepository repository;
    private final UsuarioDocumentMapper mapper;

    public UsuarioMongoAdapter(UsuarioMongoRepository repository, UsuarioDocumentMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public void guardar(Usuario usuario) {
        repository.save(mapper.aDocumento(usuario));
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByEmail(correo);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String email) {
        return repository.findByEmail(email).map(documento -> mapper.aDominio(documento));
    }
}
