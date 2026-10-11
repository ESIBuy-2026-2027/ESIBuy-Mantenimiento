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
     * Devuelve la ficha de un vendedor para supervisarlo en solo lectura (HU26).
     *
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     *         o no es un vendedor
     */
    Usuario consultarVendedor(String usuarioId);

    /**
     * Modifica los datos del usuario, y su rol si cambia, y lo devuelve ya modificado (HU22).
     *
     * @param usuarioId          id del usuario que se quiere editar
     * @param comando            datos del rol que queda
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException si los datos no son válidos
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.CambioDePropioRolException si cambia su propio rol
     */
    Usuario editar(String usuarioId, EditarUsuarioCommand comando, String emailAdministrador);

    /**
     * Desactiva la cuenta del usuario y la devuelve ya desactivada.
     *
     * @param usuarioId          id del usuario que se quiere desactivar
     * @param emailAdministrador email del administrador que hace la petición
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioNoEncontradoException si no existe
     */
    Usuario desactivar(String usuarioId, String emailAdministrador);
}
