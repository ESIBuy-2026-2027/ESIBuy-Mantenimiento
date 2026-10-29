package es.uclm.esibuy.backend.carrito.application.port.out;

/**
 * Lo que el carrito necesita del módulo de usuarios: la sesión guarda el correo del cliente, pero el carrito se
 * guarda con su id; y si es premium, para su precio.
 */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);

    boolean esPremium(String email);
}
