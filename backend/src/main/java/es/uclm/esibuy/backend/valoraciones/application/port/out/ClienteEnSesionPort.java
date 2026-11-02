package es.uclm.esibuy.backend.valoraciones.application.port.out;

/** Lo que valoraciones necesita del módulo de usuarios: la sesión guarda el correo, pero la valoración, el id. */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
