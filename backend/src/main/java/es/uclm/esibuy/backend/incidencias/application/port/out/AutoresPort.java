package es.uclm.esibuy.backend.incidencias.application.port.out;

import java.util.Optional;

import es.uclm.esibuy.backend.incidencias.domain.model.AutorIncidencia;

// Lo que incidencias necesita del módulo de usuarios para el administrador (HU49): el correo y el rol del autor.
// Vacío si la cuenta ya no existe.
public interface AutoresPort {

    Optional<AutorIncidencia> buscarAutor(String autorId);
}
