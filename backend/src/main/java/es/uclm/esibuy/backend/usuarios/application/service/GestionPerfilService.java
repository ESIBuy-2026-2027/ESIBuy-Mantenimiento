package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarPerfilCommand;
import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.PerfilUsuario;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la consulta (HU4) y la edición (HU45) del perfil propio. Solo habla con el exterior a través del
 * repositorio de usuarios. El reloj se inyecta para fijar en los tests la fecha con la que se calcula la mayoría de
 * edad.
 */
public class GestionPerfilService implements GestionPerfilUseCase {

    private static final String SESION_NO_VALIDA = "Credenciales inválidas";

    private final UsuarioRepositoryPort usuarioRepository;
    private final Clock reloj;

    public GestionPerfilService(UsuarioRepositoryPort usuarioRepository, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.reloj = reloj;
    }

    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public Usuario consultarPerfil(String email) {
        return buscarUsuarioEnSesion(email);
    }

    // El dominio (Usuario.modificarDatos) valida todo antes de asignar: si algo falla, el usuario queda como estaba
    @PreAuthorize(Permisos.CLIENTE_O_VENDEDOR_PROPIO)
    @Override
    public Usuario editarPerfil(String email, EditarPerfilCommand comando) {
        Usuario usuario = buscarUsuarioEnSesion(email);
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(),
                comando.imagenPerfil());
        PerfilUsuario perfil = perfilEditado(usuario, comando);
        if (perfil instanceof DatosVendedor datosVendedor) {
            exigirNombreComercialLibre(usuario.getDatosVendedor(), datosVendedor);
        }
        usuario.modificarDatos(datosPersonales, perfil, LocalDate.now(reloj));
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    private Usuario buscarUsuarioEnSesion(String email) {
        return usuarioRepository.buscarPorCorreo(email)
                .orElseThrow(() -> new CredencialesInvalidasException(SESION_NO_VALIDA));
    }

    // El DNI y el tipo de cliente no se editan: se copian del perfil guardado. El permiso solo deja pasar a
    // clientes y vendedores, y un cliente siempre tiene datos de cliente.
    private static PerfilUsuario perfilEditado(Usuario usuario, EditarPerfilCommand comando) {
        Telefono telefono = telefono(comando);
        if (usuario.getRol() == Rol.CLIENTE) {
            DatosCliente actuales = usuario.getDatosCliente();
            return new DatosCliente(actuales.tipoCliente(), actuales.dni(), comando.fechaNacimiento(), telefono);
        }
        DatosVendedor actuales = usuario.getDatosVendedor();
        if (actuales == null) {
            // Cuentas anteriores a la T241 o creadas por un administrador: sin DNI, que aquí no se puede poner
            throw new UsuarioInvalidoException("Un vendedor debe tener datos de vendedor");
        }
        return new DatosVendedor(comando.nombreComercial(), comando.categoriaPrincipalId(), actuales.dni(), telefono);
    }

    // Solo se comprueba si cambia: si no, el vendedor chocaría consigo mismo
    private void exigirNombreComercialLibre(DatosVendedor actuales, DatosVendedor nuevos) {
        if (!nuevos.nombreComercial().equals(actuales.nombreComercial())
                && usuarioRepository.existePorNombreComercial(nuevos.nombreComercial())) {
            throw new NombreComercialYaRegistradoException("El nombre comercial ya está registrado");
        }
    }

    // El teléfono es opcional: en blanco es que no tiene
    private static Telefono telefono(EditarPerfilCommand comando) {
        return comando.telefono() == null || comando.telefono().isBlank() ? null : new Telefono(comando.telefono());
    }
}
