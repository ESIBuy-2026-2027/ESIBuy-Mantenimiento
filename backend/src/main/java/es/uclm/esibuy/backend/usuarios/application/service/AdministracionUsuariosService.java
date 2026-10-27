package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.exception.DniYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.exception.AutoeliminacionNoPermitidaException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosVendedor;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.PerfilUsuario;
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
    public Usuario editar(String usuarioId, EditarUsuarioCommand comando) {
        Usuario usuario = buscar(usuarioId);
        DatosPersonales datosPersonales = new DatosPersonales(comando.nombre(), comando.apellidos(),
                comando.imagenPerfil());
        PerfilUsuario perfil = perfil(usuario.getRol(), comando);
        if (perfil != null) {
            exigirDatosUnicosLibres(usuario, perfil);
        }
        usuario.modificarDatos(datosPersonales, perfil, LocalDate.now(reloj));
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

    @Override
    public Usuario activarVendedor(String usuarioId) {
        Usuario vendedor = consultarVendedor(usuarioId);
        vendedor.activar();
        usuarioRepository.guardar(vendedor);
        return vendedor;
    }

    // Un vendedor nunca es el administrador en sesión ni otro administrador: no aplican las reglas de desactivar.
    @Override
    public Usuario desactivarVendedor(String usuarioId) {
        Usuario vendedor = consultarVendedor(usuarioId);
        vendedor.desactivar();
        usuarioRepository.guardar(vendedor);
        return vendedor;
    }

    @Override
    public void eliminarAdministrador(String usuarioId, String emailAdministrador) {
        Usuario administrador = buscar(usuarioId);
        if (administrador.getRol() != Rol.ADMINISTRADOR) {
            throw new UsuarioNoEncontradoException("El administrador no existe");
        }
        if (administrador.getEmail().valor().equalsIgnoreCase(emailAdministrador)) {
            throw new AutoeliminacionNoPermitidaException("Un administrador no puede eliminar su propia cuenta");
        }
        if (administrador.estaActivo() && usuarioRepository.contarAdministradoresActivos() <= 1) {
            throw new UltimoAdministradorActivoException("No se puede eliminar al último administrador activo");
        }
        usuarioRepository.eliminarPorId(usuarioId);
    }

    // Solo se comprueba lo que cambia: si no, la cuenta chocaría consigo misma. El DNI es único por rol
    // (índice rol_dni) y el nombre comercial, entre todos los vendedores.
    private void exigirDatosUnicosLibres(Usuario usuario, PerfilUsuario nuevoPerfil) {
        PerfilUsuario perfilActual = usuario.getRol() == Rol.CLIENTE
                ? usuario.getDatosCliente() : usuario.getDatosVendedor();
        Dni dniActual = perfilActual == null ? null : perfilActual.dni();
        if (!nuevoPerfil.dni().equals(dniActual)
                && usuarioRepository.existePorDniYRol(nuevoPerfil.dni().valor(), usuario.getRol())) {
            throw new DniYaRegistradoException("El DNI ya está registrado");
        }
        if (nuevoPerfil instanceof DatosVendedor nuevosDatos) {
            DatosVendedor actuales = usuario.getDatosVendedor();
            String nombreActual = actuales == null ? null : actuales.nombreComercial();
            if (!nuevosDatos.nombreComercial().equals(nombreActual)
                    && usuarioRepository.existePorNombreComercial(nuevosDatos.nombreComercial())) {
                throw new NombreComercialYaRegistradoException("El nombre comercial ya está registrado");
            }
        }
    }

    // Solo se construyen los datos del rol de la cuenta; los de otros roles se ignoran. Un administrador no tiene perfil.
    private static PerfilUsuario perfil(Rol rol, EditarUsuarioCommand comando) {
        return switch (rol) {
            case CLIENTE -> new DatosCliente(comando.tipoCliente(), new Dni(comando.dni()), comando.fechaNacimiento(),
                    telefono(comando));
            case VENDEDOR -> new DatosVendedor(comando.nombreComercial(), comando.categoriaPrincipalId(),
                    new Dni(comando.dni()), telefono(comando));
            case ADMINISTRADOR -> null;
        };
    }

    // El teléfono es opcional: en blanco (el frontend envía "") es que no tiene
    private static Telefono telefono(EditarUsuarioCommand comando) {
        return comando.telefono() == null || comando.telefono().isBlank() ? null : new Telefono(comando.telefono());
    }

    private Usuario buscar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no existe"));
    }
}
