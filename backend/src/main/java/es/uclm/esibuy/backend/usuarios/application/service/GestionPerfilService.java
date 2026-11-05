package es.uclm.esibuy.backend.usuarios.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la consulta del perfil propio (HU4) y la eliminación de la cuenta propia del cliente (HU25). Solo
 * habla con el exterior a través del repositorio de usuarios y del hash de contraseñas.
 */
public class GestionPerfilService implements GestionPerfilUseCase {

    private static final String SESION_NO_VALIDA = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;

    public GestionPerfilService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
    }

    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public Usuario consultarPerfil(String email) {
        return buscarUsuarioEnSesion(email);
    }

    @PreAuthorize(Permisos.SOLO_CLIENTE_PROPIO_POR_EMAIL)
    @Override
    public void eliminarCuenta(String email, String password) {
        Usuario cliente = buscarUsuarioEnSesion(email);
        if (!hashContrasena.coincide(password, cliente.getPasswordHash())) {
            throw new ContrasenaActualIncorrectaException("La contraseña actual no es correcta");
        }
        usuarioRepository.eliminarPorId(cliente.getId());
    }

    private Usuario buscarUsuarioEnSesion(String email) {
        return usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(SESION_NO_VALIDA));
    }
}
