package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la gestión de cuentas por el administrador. Solo habla con el exterior a través del
 * puerto de salida del repositorio; la regla de qué significa activar o desactivar vive en {@link Usuario}.
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
    public Usuario desactivar(String usuarioId, String emailAdministrador) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getEmail().valor().equalsIgnoreCase(emailAdministrador)) {
            throw new AutodesactivacionNoPermitidaException("Un administrador no puede desactivar su propia cuenta");
        }
        if (usuario.getRol() == Rol.ADMINISTRADOR && usuario.estaActivo()
                && usuarioRepository.contarAdministradoresActivos() <= 1) {
            throw new UltimoAdministradorActivoException("No se puede desactivar al último administrador activo");
        }
        usuario.desactivar();
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    private Usuario buscar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no existe"));
    }
}
