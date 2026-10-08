package es.uclm.esibuy.backend.usuarios.application.service;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;


public class RegistroUsuarioService implements RegistroUsuarioUseCase {

    private static final int LONGITUD_MINIMA_CONTRASENA = 8;
    private final UsuarioRepositoryPort usuarioRepository; //ANOTACIÓN: Repository salvando distancias es como el DAO
    private final HashContrasenaPort hashContrasena;
    
    public RegistroUsuarioService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
    }

    @Override
    public void registrar(RegistrarUsuarioCommand comando) {

        if (comando.password() == null || comando.password().length() < LONGITUD_MINIMA_CONTRASENA) {
            throw new ContrasenaInvalidaException("La contraseña debe tener mínimo " + LONGITUD_MINIMA_CONTRASENA + " caracteres"); // REVISAR: Aquí deberia ser mensaje poco explicativo no?         
        }
        if (usuarioRepository.existePorCorreo(comando.email())) {
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }

        String contrasenaHash = hashContrasena.hashear(comando.password());   
        Usuario usuario = Usuario.nuevoCliente(new Email(comando.email()), contrasenaHash); // Comando → Usuario: el comando es un DTO de entrada, el usuario es un objeto de dominio
        usuarioRepository.guardar(usuario);
    }

}
