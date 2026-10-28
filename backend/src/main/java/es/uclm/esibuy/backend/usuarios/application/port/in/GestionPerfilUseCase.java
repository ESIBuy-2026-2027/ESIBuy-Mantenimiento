package es.uclm.esibuy.backend.usuarios.application.port.in;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

/**
 * Puerto de entrada del cliente o el vendedor sobre su propio perfil (HU4). La edición del perfil (HU45) se
 * añadirá aquí.
 */
public interface GestionPerfilUseCase {

    /**
     * Devuelve el usuario en sesión, identificado por su correo. Si ya no existe, la sesión no es válida
     * ({@code CredencialesInvalidasException}, 401), como en {@code /api/auth/me}.
     */
    Usuario consultarPerfil(String email);
}
