package es.uclm.esibuy.backend.valoraciones.application.port.in;

import es.uclm.esibuy.backend.valoraciones.domain.model.Valoracion;

/**
 * Valoración de productos por parte del cliente (HU13): solo puede valorar lo que ha comprado, una vez por
 * producto. El cliente sale siempre de la sesión.
 */
public interface ValoracionProductosUseCase {

    /** Crea y guarda la valoración del cliente. Devuelve la valoración guardada, con su id. */
    Valoracion valorar(ValorarProductoCommand comando);
}
