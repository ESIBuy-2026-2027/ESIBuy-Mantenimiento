package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;



public class RegistroUsuarioService {

    private  final UsuarioRepositoryPort usuarioRepository; //ANOTACIÓN: Repository salvando distancias es como el DAO

    public RegistroUsuarioService(UsuarioRepositoryPort usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public void registrar(RegistrarUsuarioCommand comando) {
        Usuario usuario = new Usuario(comando.correo(), comando.contrasena(), Rol.CLIENTE); // Comando → Usuario: el comando es un DTO de entrada, el usuario es un objeto de dominio
        usuarioRepository.guardar(usuario);
    }

}
