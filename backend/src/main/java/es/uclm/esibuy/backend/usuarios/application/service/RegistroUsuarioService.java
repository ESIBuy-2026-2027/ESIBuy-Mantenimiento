package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PoliticaContrasenas;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Caso de uso de registro de un cliente (HU1).
 *
 * <p>Orquesta el dominio: construye los value objects a partir del comando (y con ello aplica sus
 * reglas), comprueba lo que necesita del exterior a través de los puertos y guarda el usuario.
 * El reloj se inyecta para que la fecha de hoy y el instante del consentimiento se puedan fijar
 * en los tests.
 */
public class RegistroUsuarioService implements RegistroUsuarioUseCase {

    // Versión fija de la política de privacidad mientras no se gestione desde la aplicación (decisión del 2026-10-08).
    private static final String VERSION_POLITICA_PRIVACIDAD = "1.0";

    private final UsuarioRepositoryPort usuarioRepository;
    private final HashContrasenaPort hashContrasena;
    private final Clock reloj;

    public RegistroUsuarioService(UsuarioRepositoryPort usuarioRepository, HashContrasenaPort hashContrasena,
            Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.hashContrasena = hashContrasena;
        this.reloj = reloj;
    }

    @Override
    public void registrar(RegistrarUsuarioCommand comando) {
        // Todo lo que se puede comprobar sin salir del núcleo va antes de consultar la base de datos.
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(), comando.imagenPerfil());
        Email email = new Email(comando.email());
        PoliticaContrasenas.validar(comando.password(), datosPersonales, email);   // también rechaza la contraseña nula
        if (!comando.password().equals(comando.repeticionPassword())) {
            throw new ContrasenaInvalidaException("Las contraseñas no coinciden");
        }
        if (!comando.aceptaPrivacidad()) {
            throw new UsuarioInvalidoException("Es obligatorio aceptar la política de privacidad");
        }
        if (usuarioRepository.existePorCorreo(email.valor())) {   // con el email normalizado (T240)
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
        if (usuarioRepository.existePorDni(comando.dni())) {
            throw new DniYaRegistradoException("El DNI ya está registrado");
        }

        String contrasenaHash = hashContrasena.hashear(comando.password());
        Usuario usuario = Usuario.nuevoCliente(
                datosPersonales,
                email,
                contrasenaHash,
                new DatosCliente(comando.tipoCliente(), new Dni(comando.dni()), comando.fechaNacimiento(),
                        comando.telefono() == null ? null : new Telefono(comando.telefono())),
                new ConsentimientoPrivacidad(reloj.instant(), VERSION_POLITICA_PRIVACIDAD),
                LocalDate.now(reloj));
        usuarioRepository.guardar(usuario);
    }
}
