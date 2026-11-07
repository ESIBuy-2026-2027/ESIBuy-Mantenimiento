package es.uclm.esibuy.backend.incidencias.application.port.out;

/** Lo que incidencias necesita del módulo de usuarios: la sesión guarda el correo, pero la incidencia, el id. */
public interface AutorEnSesionPort {

    String idPorCorreo(String email);
}