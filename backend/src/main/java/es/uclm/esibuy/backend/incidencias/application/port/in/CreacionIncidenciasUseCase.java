package es.uclm.esibuy.backend.incidencias.application.port.in;

import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/**
 * Creación de incidencias por parte del cliente y del vendedor (HU44). El autor sale siempre de la sesión; el
 * administrador no crea incidencias, las revisa (HU49 y HU50).
 */
public interface CreacionIncidenciasUseCase {

    /** Crea y guarda la incidencia del usuario en sesión. Devuelve la incidencia guardada, con su id. */
    Incidencia crear(CrearIncidenciaCommand comando);
}