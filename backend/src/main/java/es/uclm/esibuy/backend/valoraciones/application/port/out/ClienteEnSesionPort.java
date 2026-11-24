package es.uclm.esibuy.backend.valoraciones.application.port.out;

/** Datos mínimos del cliente autenticado que necesita el módulo de valoraciones. */
public interface ClienteEnSesionPort {

    String idPorCorreo(String email);

    String nombreCompletoPorCorreo(String email);
}
