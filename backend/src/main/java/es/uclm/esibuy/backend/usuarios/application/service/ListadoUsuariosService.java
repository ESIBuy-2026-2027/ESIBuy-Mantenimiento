package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.ListadoUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;

/**
 * Implementa el caso de uso de listado sin acoplarlo a la tecnología de persistencia.
 */
public class ListadoUsuariosService implements ListadoUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public ListadoUsuariosService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioPage listar(ListarUsuariosQuery query) {
        if (query == null) {
            throw new IllegalArgumentException("La consulta es obligatoria");
        }
        // El servicio orquesta el caso de uso; la búsqueda y la paginación
        // pertenecen al adaptador que conoce MongoDB.
        return usuarioRepository.listar(query);
    }
}
