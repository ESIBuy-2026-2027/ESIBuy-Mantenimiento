package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import java.time.DateTimeException;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;
import es.uclm.esibuy.backend.estadisticas.domain.model.PeriodoEstadisticasVendedor;

/**
 * Adaptador de entrada web de las estadísticas del vendedor (HU17). /api/seller/** solo lo usa un VENDEDOR
 * (SecurityConfig, T57).
 */
@RestController
@RequestMapping("/api")
public class EstadisticasVendedorController {

    private final EstadisticasVendedorUseCase estadisticasVendedor;

    public EstadisticasVendedorController(EstadisticasVendedorUseCase estadisticasVendedor) {
        this.estadisticasVendedor = estadisticasVendedor;
    }

    // El vendedor sale de la sesión, nunca de un parámetro
    @GetMapping("/seller/statistics")
    public EstadisticasVendedorResponse consultar(Authentication autenticado,
            @RequestParam(defaultValue = "TODO_EL_PERIODO") PeriodoEstadisticasVendedor periodo,
            @RequestParam(defaultValue = "UTC") String zonaHoraria) {
        return EstadisticasVendedorResponse.desde(
                estadisticasVendedor.consultar(autenticado.getName(), periodo, zonaValida(zonaHoraria)), periodo);
    }

    private static ZoneId zonaValida(String zonaHoraria) {
        try {
            return ZoneId.of(zonaHoraria);
        } catch (DateTimeException excepcion) {
            return ZoneOffset.UTC;
        }
    }
}
