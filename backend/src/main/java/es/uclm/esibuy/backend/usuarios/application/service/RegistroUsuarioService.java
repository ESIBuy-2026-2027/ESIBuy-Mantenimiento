package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarClienteCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistrarVendedorCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.RegistroUsuarioUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.HashContrasenaPort;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaInvalidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYNombreComercialYaRegistradosException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CorreoYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.MotivoRechazo;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.ConsentimientoPrivacidad;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.PoliticaContrasenas;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;

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

    @PreAuthorize(Permisos.PUBLICO)
    @Override
    public void registrarCliente(RegistrarClienteCommand comando) {
        // Todo lo que se puede comprobar sin salir del núcleo va antes de consultar la base de datos.
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(), comando.imagenPerfil());
        Email email = new Email(comando.email());
        exigirContrasenaValida(comando.password(), comando.repeticionPassword(), datosPersonales, email);
        exigirPrivacidadAceptada(comando.aceptaPrivacidad());
        if (usuarioRepository.existePorCorreo(email.valor())) {   // con el email normalizado (T240)
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
        exigirDniLibre(comando.dni(), Rol.CLIENTE);

        Usuario usuario = Usuario.nuevoCliente(
                datosPersonales,
                email,
                hashContrasena.hashear(comando.password()),
                new DatosCliente(comando.tipoCliente(), new Dni(comando.dni()), comando.fechaNacimiento(),
                        telefono(comando.telefono())),
                consentimientoAhora(),
                LocalDate.now(reloj));
        usuarioRepository.guardar(usuario);
    }

    // Registro del vendedor (T241): las mismas reglas que el cliente, sin mayoría de edad y con nombre comercial.
    @PreAuthorize(Permisos.PUBLICO)
    @Override
    public void registrarVendedor(RegistrarVendedorCommand comando) {
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(), comando.imagenPerfil());
        Email email = new Email(comando.email());
        exigirContrasenaValida(comando.password(), comando.repeticionPassword(), datosPersonales, email);
        exigirPrivacidadAceptada(comando.aceptaPrivacidad());
        exigirCorreoYNombreComercialLibres(email, comando.nombreComercial());
        exigirDniLibre(comando.dni(), Rol.VENDEDOR);

        Usuario usuario = Usuario.nuevoVendedor(
                datosPersonales,
                email,
                hashContrasena.hashear(comando.password()),
                new DatosVendedor(comando.nombreComercial(), comando.categoriaPrincipalId(), new Dni(comando.dni()),
                        telefono(comando.telefono())),
                consentimientoAhora());
        usuarioRepository.guardar(usuario);
    }

    private static void exigirContrasenaValida(String password, String repeticion, DatosPersonales datosPersonales,
            Email email) {
        PoliticaContrasenas.validar(password, datosPersonales, email);   // también rechaza la contraseña nula
        if (!password.equals(repeticion)) {
            throw new ContrasenaInvalidaException(MotivoRechazo.CONTRASENAS_DISTINTAS, "Las contraseñas no coinciden");
        }
    }

    private static void exigirPrivacidadAceptada(boolean aceptaPrivacidad) {
        if (!aceptaPrivacidad) {
            throw new UsuarioInvalidoException(MotivoRechazo.PRIVACIDAD_NO_ACEPTADA,
                    "Es obligatorio aceptar la política de privacidad");
        }
    }

    // Se consultan los dos antes de decidir: si se repiten ambos, el frontend espera un único 409 que lo diga.
    private void exigirCorreoYNombreComercialLibres(Email email, String nombreComercial) {
        boolean correoRepetido = usuarioRepository.existePorCorreo(email.valor());
        boolean nombreComercialRepetido = usuarioRepository.existePorNombreComercial(nombreComercial);
        if (correoRepetido && nombreComercialRepetido) {
            throw new CorreoYNombreComercialYaRegistradosException("El correo y el nombre comercial ya están registrados");
        }
        if (correoRepetido) {
            throw new CorreoYaRegistradoException("El correo ya está registrado");
        }
        if (nombreComercialRepetido) {
            throw new NombreComercialYaRegistradoException("El nombre comercial ya está registrado");
        }
    }

    // El DNI es único por rol: el mismo DNI puede tener una cuenta de cliente y otra de vendedor.
    private void exigirDniLibre(String dni, Rol rol) {
        if (usuarioRepository.existePorDniYRol(dni, rol)) {
            throw new DniYaRegistradoException("El DNI ya está registrado");
        }
    }

    private static Telefono telefono(String telefono) {
        return telefono == null ? null : new Telefono(telefono);
    }

    private ConsentimientoPrivacidad consentimientoAhora() {
        return new ConsentimientoPrivacidad(reloj.instant(), VERSION_POLITICA_PRIVACIDAD);
    }
}
