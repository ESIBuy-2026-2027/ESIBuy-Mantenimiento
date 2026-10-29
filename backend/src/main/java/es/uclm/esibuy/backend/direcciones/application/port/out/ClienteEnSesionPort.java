package es.uclm.esibuy.backend.direcciones.application.port.out;

/**
 * Lo que direcciones necesita del módulo de usuarios: la sesión guarda el correo del cliente, pero las
 * direcciones se guardan con su id.
 */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
