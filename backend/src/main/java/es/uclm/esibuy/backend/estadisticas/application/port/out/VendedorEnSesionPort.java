package es.uclm.esibuy.backend.estadisticas.application.port.out;

/**
 * Lo que estadísticas necesita del módulo de usuarios: la sesión guarda el correo del vendedor, pero los pedidos y
 * los productos se guardan con su id.
 */
public interface VendedorEnSesionPort {

    String idPorCorreo(String email);
}
