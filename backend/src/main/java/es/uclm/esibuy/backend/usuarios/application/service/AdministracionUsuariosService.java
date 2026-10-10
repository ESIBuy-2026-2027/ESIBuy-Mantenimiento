package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la gestión de cuentas por el administrador. Solo habla con el exterior a través del
 * puerto de salida del repositorio; la regla de qué significa activar vive en {@link Usuario}.
 */
public class AdministracionUsuariosService implements AdministracionUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepository;

    public AdministracionUsuariosService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario activar(String usuarioId) {
        Usuario usuario = buscar(usuarioId);
        usuario.activar();
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    @Override
    public Usuario consultar(String usuarioId) {
        return buscar(usuarioId);
    }

    private Usuario buscar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no existe"));
    }
}
