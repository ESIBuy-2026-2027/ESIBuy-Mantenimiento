package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Puerto de entrada del cliente o el vendedor sobre su propio perfil: consultarlo (HU4) y editarlo (HU45). Incluye
 * también la eliminación de la cuenta propia del cliente (HU25).
 */
public interface GestionPerfilUseCase {

    /**
     * Devuelve el usuario en sesión, identificado por su correo. Si ya no existe, la sesión no es válida
     * ({@code CredencialesInvalidasException}, 401), como en {@code /api/auth/me}.
     */
    Usuario consultarPerfil(String email);

    /**
     * Edita los datos modificables del perfil del cliente o el vendedor en sesión (HU45) y devuelve el usuario
     * actualizado. El email, el DNI y el tipo de cliente se conservan. Si algo no es válido, no se guarda nada.
     *
     * @param email   correo de la sesión: solo se puede editar el perfil propio
     * @param comando datos nuevos del perfil
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException si algún dato no es
     *         válido, si el cliente quedaría menor de edad o si el vendedor no tiene datos de vendedor (su DNI no
     *         se puede poner desde aquí)
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.NombreComercialYaRegistradoException si el nuevo
     *         nombre comercial ya lo usa otro vendedor
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException si el usuario de
     *         la sesión ya no existe
     */
    Usuario editarPerfil(String email, EditarPerfilCommand comando);

    /**
     * Elimina la cuenta del cliente en sesión (HU25), tras confirmar su contraseña actual. El borrado es físico,
     * como el de un administrador (HU37).
     *
     * @param email    correo de la sesión: solo se puede eliminar la cuenta propia
     * @param password contraseña actual del cliente
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.ContrasenaActualIncorrectaException si la
     *         contraseña no coincide; no se borra nada
     * @throws es.uclm.esibuy.backend.usuarios.domain.exception.CredencialesInvalidasException si el usuario de
     *         la sesión ya no existe
     */
    void eliminarCuenta(String email, String password);
}
