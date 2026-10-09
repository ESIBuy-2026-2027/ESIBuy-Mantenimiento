package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;

/**
 * Puerto de entrada para consultar el listado administrativo de usuarios.
 */
public interface ListadoUsuariosUseCase {

    /**
     * Devuelve la página que cumple los criterios de la consulta.
     *
     * @param query criterios de búsqueda, filtrado y paginación
     * @return resultado paginado de usuarios
     */
    UsuarioPage listar(ListarUsuariosQuery query);

}
