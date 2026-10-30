package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Casos de uso con los que el administrador gestiona las cuentas de los usuarios.
 *
 * <p>Agrupa las operaciones de un mismo actor con un mismo propósito (CLAUDE.md): de momento,
 * activar una cuenta (HU24), consultar su ficha (HU20) y la de un vendedor (HU26), desactivarla (HU23)
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
     * Modifica los datos del usuario y lo devuelve ya modificado (HU22).
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException si los datos no son válidos
     */
    Usuario editar(String usuarioId, EditarUsuarioCommand comando);

    /**
     * Desactiva la cuenta del usuario y la devuelve ya desactivada.
     *
     * @param usuarioId          id del usuario que se quiere desactivar
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     */
    Usuario desactivar(String usuarioId, String emailAdministrador);

    /**
     * Activa la cuenta de un vendedor desde su supervisión (HU21) y la devuelve ya activada.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario activarVendedor(String usuarioId);

    /**
     * Desactiva la cuenta de un vendedor desde su supervisión (HU21) y la devuelve ya desactivada.
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario desactivarVendedor(String usuarioId);

    /**
     * Elimina físicamente la cuenta de un administrador (HU37).
     *
     * @param usuarioId          id del administrador que se quiere eliminar
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un administrador
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.AutodesactivacionNoPermitidaException si intenta eliminarse a sí mismo
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UltimoAdministradorActivoException si es el último administrador activo
     */
    void eliminarAdministrador(String usuarioId, String emailAdministrador);

    
}
