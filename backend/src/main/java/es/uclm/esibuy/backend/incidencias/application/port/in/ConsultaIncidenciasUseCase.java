package es.uclm.esibuy.backend.incidencias.application.port.in;

import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/**
 * Consulta de incidencias por parte del cliente y del vendedor (HU44): cada uno ve las suyas, con su estado y la
 * respuesta del administrador. El administrador las ve todas por otra vía (HU49).
 */
public interface ConsultaIncidenciasUseCase {

    /**
     * Las incidencias del usuario en sesión, de las más antiguas a las más recientes, por páginas (la primera es la
     * 0, y el tamaño va de 1 a 50).
     */
    IncidenciaPage consultarPropias(String email, int pagina, int tamano);
}