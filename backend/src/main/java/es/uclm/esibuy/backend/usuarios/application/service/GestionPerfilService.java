package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/** Implementa la consulta del perfil propio. Solo habla con el exterior a través del repositorio de usuarios. */
public class GestionPerfilService implements GestionPerfilUseCase {

    private static final String SESION_NO_VALIDA = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;

    public GestionPerfilService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public Usuario consultarPerfil(String email) {
        return usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(SESION_NO_VALIDA));
    }
}
