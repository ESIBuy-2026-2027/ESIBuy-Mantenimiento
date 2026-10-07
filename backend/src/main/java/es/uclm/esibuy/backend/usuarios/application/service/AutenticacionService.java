package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.IniciarSesionCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.PasoLogin;
import es.uclm.esibuy.backend.usuarios.application.port.in.ResultadoInicioSesion;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public class AutenticacionService implements AutenticacionUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;

    public AutenticacionService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
    }

    @Override
    public ResultadoInicioSesion iniciarSesion(IniciarSesionCommand comando) {
        
        Usuario usuario = usuarioRepository.buscarPorCorreo(comando.email()).orElseThrow(); // <- Este orElseThrow() viene de buscarPorCorreo() que devuelve Optional<>

        if (!hashContrasena.coincide(comando.password(), usuario.getPasswordHash())) {
            throw new UnsupportedOperationException("Por implementar");
        }

        return new ResultadoInicioSesion(usuario.getEmail(), PasoLogin.CODIGO_EMAIL);
    }

    

}
