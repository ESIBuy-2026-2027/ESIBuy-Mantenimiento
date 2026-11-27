package es.uclm.esibuy.backend.valoraciones.application.port.in;

import java.time.Instant;
import java.util.Optional;

import es.uclm.esibuy.backend.valoraciones.domain.model.ResumenValoracionesVendedor;

/** Estadísticas de valoraciones de los productos propios del vendedor en sesión (HU17). */
public interface EstadisticasValoracionesVendedorUseCase {

    ResumenValoracionesVendedor consultar(String emailVendedor, Optional<Instant> desde, Instant hasta);
}
