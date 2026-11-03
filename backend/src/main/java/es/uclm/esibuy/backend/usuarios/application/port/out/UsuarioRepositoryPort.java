package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.application.port.in.ListarUsuariosQuery;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;
import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;

public interface UsuarioRepositoryPort {

    void guardar(Usuario usuario);

    boolean existePorCorreo(String email);

    // El DNI es único por rol (índice rol_dni): una persona puede tener cuenta de cliente y de vendedor.
    boolean existePorDniYRol(String dni, Rol rol);

    boolean existePorNombreComercial(String nombreComercial);

/**
 * Busca un usuario por su correo.
 *
 * <p>Devuelve {@link Optional} en lugar de {@code null}: la firma deja claro que el usuario
 * puede no existir y el compilador obliga a tratar ese caso antes de usarlo
 * (p. ej. {@code orElseThrow(CredencialesInvalidasException::new)} en el login).
 * Con {@code null}, olvidar la comprobación provocaría un {@code NullPointerException}
 * en ejecución.
 */
    Optional<Usuario> buscarPorCorreo(String email);

    // Busca un usuario por su id (p. ej. para activar su cuenta, HU24). Vacío si no existe.
    Optional<Usuario> buscarPorId(String id);

    // Cuenta los administradores en estado ACTIVO (p. ej. para no bloquear al último, HU23).
    long contarAdministradoresActivos();

    UsuarioPage listar(ListarUsuariosQuery query);

    // Elimina físicamente un usuario por su id (HU37).
    void eliminarPorId(String id);

    // ¿Hay algún vendedor cuya categoría principal sea esta? (HU42)
    boolean existeVendedorConCategoriaPrincipal(String categoriaId);
}
