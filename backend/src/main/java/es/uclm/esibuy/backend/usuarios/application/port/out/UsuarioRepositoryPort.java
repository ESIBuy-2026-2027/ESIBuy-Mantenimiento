package es.uclm.esibuy.backend.usuarios.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

public interface UsuarioRepositoryPort {

    void guardar(Usuario usuario);

    boolean existePorCorreo(String email);

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

}
