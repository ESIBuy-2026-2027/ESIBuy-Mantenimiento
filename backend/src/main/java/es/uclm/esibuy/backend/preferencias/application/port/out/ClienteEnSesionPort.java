package es.uclm.esibuy.backend.preferencias.application.port.out;

/** Lo que las preferencias necesitan del módulo de usuarios: la sesión guarda el correo, pero las preferencias, el id. */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
