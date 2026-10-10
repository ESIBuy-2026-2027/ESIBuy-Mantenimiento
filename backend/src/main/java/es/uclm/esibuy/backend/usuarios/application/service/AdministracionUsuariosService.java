package es.uclm.esibuy.backend.usuarios.application.service;

import java.time.Clock;
import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.application.port.in.AdministracionUsuariosUseCase;
import es.uclm.esibuy.backend.usuarios.application.port.in.EditarUsuarioCommand;
import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Telefono;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Implementa la gestión de cuentas por el administrador. Solo habla con el exterior a través del
 * puerto de salida del repositorio; la regla de qué significa activar vive en {@link Usuario}.
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
        // Los datos de cliente solo se construyen para un cliente; los de otros roles se ignoran.
        DatosCliente datosCliente = usuario.getRol() == Rol.CLIENTE
                ? new DatosCliente(comando.tipoCliente(), new Dni(comando.dni()), comando.fechaNacimiento(),
                        comando.telefono() == null ? null : new Telefono(comando.telefono()))
                : null;
        usuario.modificarDatos(datosPersonales, datosCliente, LocalDate.now(reloj));
        usuarioRepository.guardar(usuario);
        return usuario;
    }

    private Usuario buscar(String usuarioId) {
        return usuarioRepository.buscarPorId(usuarioId)
                .orElseThrow(() -> new UsuarioNoEncontradoException("El usuario no existe"));
    }
}
