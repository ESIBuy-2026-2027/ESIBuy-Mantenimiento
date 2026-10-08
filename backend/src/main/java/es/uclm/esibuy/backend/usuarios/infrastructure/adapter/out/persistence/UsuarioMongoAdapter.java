package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import org.springframework.dao.DuplicateKeyException;

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
        UsuarioDocument documento = mapper.aDocumento(usuario);
        if (usuario.getId() != null) {
            // Actualización: el documento se rehace desde el dominio, que no conoce la fecha de creación.
            repository.findById(usuario.getId())
                    .ifPresent(guardado -> documento.setCreatedAt(guardado.getCreatedAt()));
        }
        try {
            repository.save(documento);
        } catch (DuplicateKeyException _) {
            // El índice único de email lo ha rechazado (p. ej. dos registros simultáneos que pasaron
            // a la vez la comprobación de existePorCorreo). Se traduce a la excepción del dominio,
            // que la API devuelve como 409, para que el núcleo no dependa de MongoDB.
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
    }

    @Override
    public boolean existePorCorreo(String correo) {
        return repository.existsByEmail(correo);
    }

    @Override
    public Optional<Usuario> buscarPorCorreo(String email) {
        return repository.findByEmail(email).map(mapper::aDominio);
    }
}
