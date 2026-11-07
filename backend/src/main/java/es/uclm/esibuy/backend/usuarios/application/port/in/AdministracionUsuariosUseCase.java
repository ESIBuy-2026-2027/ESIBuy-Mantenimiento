package es.uclm.esibuy.backend.usuarios.application.port.in;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Casos de uso con los que el administrador gestiona las cuentas de los usuarios.
 *
 * <p>Agrupa las operaciones de un mismo actor con un mismo propósito (CLAUDE.md): de momento,
 * activar una cuenta (HU24), consultar su ficha (HU20) y la de un vendedor (HU26), bloquearla (HU23)
 * y editar sus datos (HU22).
 */
public interface AdministracionUsuariosUseCase {

    /**
     * Activa la cuenta del usuario y la devuelve ya activada.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     */
    Usuario activar(String usuarioId);

    /**
     * Devuelve el usuario para ver su ficha (HU20).
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     */
    Usuario consultar(String usuarioId);

    /**
     * Devuelve la ficha de un vendedor para supervisarlo (HU26). La ficha se consulta; los datos de su cuenta se
     * modifican con el resto de operaciones de este puerto (HU22, HU23 y HU24).
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario consultarVendedor(String usuarioId);

    /**
     * Solo el nombre comercial del vendedor, para otros módulos (p. ej. el carrito, HU9): así no ven el dominio
     * de usuarios.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    String consultarNombreComercial(String vendedorId);

    /**
     * Solo el correo y el rol del usuario, para la supervisión de incidencias (HU49): así no ve el dominio de
     * usuarios. Si el usuario no existe (p. ej. una cuenta eliminada), devuelve vacío en vez de fallar.
     */
    Optional<ContactoUsuario> consultarContacto(String usuarioId);

    /**
     * Modifica los datos del usuario y lo devuelve ya modificado (HU22).
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException si los datos no son válidos
     */
    Usuario editar(String usuarioId, EditarUsuarioCommand comando);

    /**
     * Bloquea la cuenta del usuario y la devuelve ya bloqueada.
     *
     * @param usuarioId          id del usuario que se quiere bloquear
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     */
    Usuario bloquear(String usuarioId, String emailAdministrador);

    /**
     * Activa la cuenta de un vendedor desde su supervisión (HU21) y la devuelve ya activada.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario activarVendedor(String usuarioId);

    /**
     * Bloquea la cuenta de un vendedor desde su supervisión (HU21) y la devuelve ya bloqueada.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario bloquearVendedor(String usuarioId);

    /**
     * Elimina físicamente la cuenta de un administrador (HU37).
     *
     * @param usuarioId          id del administrador que se quiere eliminar
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un administrador
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.AutobloqueoNoPermitidoException si intenta eliminarse a sí mismo
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException si es el último administrador activo
     */
    void eliminarAdministrador(String usuarioId, String emailAdministrador);

    /**
     * ¿Algún vendedor tiene esta categoría como principal? Lo usa el módulo de categorías (HU42) para no eliminar
     * una categoría en uso, sin ver el dominio de usuarios.
     */
    boolean existenVendedoresConCategoria(String categoriaId);
    
}
