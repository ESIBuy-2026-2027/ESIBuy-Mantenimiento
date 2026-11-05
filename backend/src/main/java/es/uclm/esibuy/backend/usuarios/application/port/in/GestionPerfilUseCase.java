package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Puerto de entrada del cliente o el vendedor sobre su propio perfil (HU4) y de la eliminación de la cuenta
 * propia del cliente (HU25). La edición del perfil (HU45) se añadirá aquí.
 */
public interface GestionPerfilUseCase {

    /**
     * Devuelve el usuario en sesión, identificado por su correo. Si ya no existe, la sesión no es válida
     * ({@code CredencialesInvalidasException}, 401), como en {@code /api/auth/me}.
     */
    Usuario consultarPerfil(String email);

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
