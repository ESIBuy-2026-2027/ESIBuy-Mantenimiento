package es.uclm.esibuy.backend.metodospago.application.port.out;

/**
 * Lo que metodospago necesita del módulo de usuarios: la sesión guarda el correo del cliente, pero los
 * métodos de pago se guardan con su id.
 */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
