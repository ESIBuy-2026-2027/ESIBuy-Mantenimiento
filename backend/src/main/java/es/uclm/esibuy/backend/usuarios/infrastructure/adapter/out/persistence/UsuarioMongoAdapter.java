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

    // PENDIENTE (T216, David): buscarPorCorreo lo añadió el login (T19) al puerto después de crear este
    // adaptador. Falta la lectura desde MongoDB: findByEmail en UsuarioMongoRepository y el mapeo
    // documento → Usuario en UsuarioDocumentMapper, con su test de integración.
    // Mientras tanto, el login solo se prueba con mocks y no llega a ejecutar este método.
    @Override
    public Optional<Usuario> buscarPorCorreo(String email) {
        throw new UnsupportedOperationException("Pendiente: lectura de usuarios desde MongoDB (T216)");
    }
}
