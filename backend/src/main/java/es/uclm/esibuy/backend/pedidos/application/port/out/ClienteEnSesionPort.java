package es.uclm.esibuy.backend.pedidos.application.port.out;

/** Lo que pedidos necesita del módulo de usuarios: la sesión guarda el correo, pero el pedido se guarda con el id. */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
