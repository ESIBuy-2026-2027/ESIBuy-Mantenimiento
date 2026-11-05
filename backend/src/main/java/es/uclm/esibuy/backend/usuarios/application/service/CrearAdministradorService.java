package es.uclm.esibuy.backend.usuarios.application.service;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.CrearAdministradorUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PoliticaContrasenas;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

public class CrearAdministradorService implements CrearAdministradorUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;

    public CrearAdministradorService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena; 
    }

    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public void crear(CrearAdministradorCommand comando) {
        // Lo que se puede comprobar sin salir del núcleo va antes de consultar la base de datos.
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(), comando.avatar());
        Email email = new Email(comando.email());
        
        // Reutilizamos la clase de dominio de contraseñas
        PoliticaContrasenas.validar(comando.password(), datosPersonales, email);

        if (usuarioRepository.existePorCorreo(comando.email())) {
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }

        String contrasenaHash = hashContrasena.hashear(comando.password());
        
        Usuario nuevoAdmin = Usuario.nuevoAdministrador(
                datosPersonales,
                email,
                contrasenaHash
        );
        
        usuarioRepository.guardar(nuevoAdmin);
    }
}