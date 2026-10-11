package es.uclm.esibuy.backend.productos.application.port.out;

/**
 * Lo que productos necesita del módulo de usuarios: la sesión guarda el correo del usuario, pero
 * los productos se guardan con el id de su vendedor.
 */
public interface UsuarioEnSesionPort {

    String idPorCorreo(String email);
}
