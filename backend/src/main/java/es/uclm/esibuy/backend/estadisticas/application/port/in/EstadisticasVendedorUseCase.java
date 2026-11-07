package es.uclm.esibuy.backend.estadisticas.application.port.in;

import es.uclm.esibuy.backend.estadisticas.domain.model.EstadisticasVendedor;

/** Consulta de las estadísticas de ventas, valoraciones y stock del vendedor en sesión (HU17). */
public interface EstadisticasVendedorUseCase {

    /** Las estadísticas del vendedor con ese correo, que debe ser el de la sesión. */
    EstadisticasVendedor consultar(String emailVendedor);
}
