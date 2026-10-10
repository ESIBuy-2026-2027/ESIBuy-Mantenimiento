package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.CambioDePropioRolException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la gestión de cuentas por el administrador. Solo habla con el exterior a través del
 * puerto de salida del repositorio; la regla de qué significa activar o desactivar vive en {@link Usuario}.
 * El reloj se inyecta para fijar en los tests la fecha con la que se calcula la mayoría de edad.
 */
public class AdministracionUsuariosService implements AdministracionUsuariosUseCase {

    private final UsuarioRepositoryPort usuarioRepository;
    private final Clock reloj;

    public AdministracionUsuariosService(UsuarioRepositoryPort usuarioRepository, Clock reloj) {
        this.usuarioRepository = usuarioRepository;
        this.reloj = reloj;
    }

    @Override
    public Usuario activar(String usuarioId) {
        Usuario usuario = buscar(usuarioId);
        usuario.activar();
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    @Override
    public Usuario consultar(String usuarioId) {
        return buscar(usuarioId);
    }

    @Override
    public Usuario consultarVendedor(String usuarioId) {
        Usuario usuario = buscar(usuarioId);
        if (usuario.getRol() != Rol.VENDEDOR) {
            // Por la zona de vendedores solo se ven vendedores: otro usuario es como si no existiera.
            throw new UsuarioNoEncontradoException("El vendedor no existe");
        }
        return usuario;
    }

    @Override
    public Usuario editar(String usuarioId, EditarUsuarioCommand comando, String emailAdministrador) {
        Usuario usuario = buscar(usuarioId);
        // Puede editar sus propios datos, pero no degradarse a sí mismo
        if (comando.rol() != usuario.getRol() && usuario.getEmail().valor().equalsIgnoreCase(emailAdministrador)) {
            throw new CambioDePropioRolException("Un administrador no puede cambiar su propio rol");
        }
        // PENDIENTE: pasar una cuenta a vendedor, cuando existan sus datos (nombre comercial, NIF y categoría).
        if (comando.rol() == Rol.VENDEDOR && usuario.getRol() != Rol.VENDEDOR) {
            throw new UsuarioInvalidoException("Pasar una cuenta a vendedor todavía no está disponible");
        }
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(),
                comando.imagenPerfil());
        // Los datos son los del rol que queda (el nuevo si cambia)
        DatosCliente datosCliente = datosCliente(comando.rol(), comando);
        if (datosCliente != null) {
            exigirDniLibre(usuario, datosCliente.dni());
        }
        usuario.cambiarRol(comando.rol(), datosPersonales, datosCliente, LocalDate.now(reloj));
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

    // Solo se comprueba si el DNI cambia: si no, el cliente chocaría consigo mismo. Quien pasa a cliente no tiene DNI.
    // PENDIENTE: existePorDni no distingue rol, aunque el índice rol_dni sí; revisar cuando los vendedores tengan DNI.
    private void exigirDniLibre(Usuario usuario, Dni nuevoDni) {
        DatosCliente actuales = usuario.getDatosCliente();
        boolean esSuDni = actuales != null && nuevoDni.equals(actuales.dni());
        if (!esSuDni && usuarioRepository.existePorDni(nuevoDni.valor())) {
            throw new DniYaRegistradoException("El DNI ya está registrado");
        }
    }

    // Los datos de cliente solo se construyen para un cliente; en otros roles se ignoran.
    private static DatosCliente datosCliente(Rol rol, EditarUsuarioCommand comando) {
        if (rol != Rol.CLIENTE) {
            return null;
        }
        // El teléfono es opcional: en blanco (el frontend envía "") es que no tiene
        Telefono telefono = comando.telefono() == null || comando.telefono().isBlank()
                ? null : new Telefono(comando.telefono());
        return new DatosCliente(comando.tipoCliente(), new Dni(comando.dni()), comando.fechaNacimiento(), telefono);
    }

    private Usuario buscar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no existe"));
    }
}
