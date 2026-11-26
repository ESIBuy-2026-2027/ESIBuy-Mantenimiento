package es.uclm.esibuy.backend.alertas.application.port.out;

/** Lo que las alertas necesitan del cliente en sesión (módulo de usuarios). */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);
}
