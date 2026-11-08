package es.uclm.esibuy.backend.deseos.application.port.out;

/**
 * Lo que la lista de deseos necesita del módulo de usuarios: la sesión guarda el correo, pero la lista, el id; y si
 * es premium, para su precio.
 */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);

    boolean esPremium(String email);
}
