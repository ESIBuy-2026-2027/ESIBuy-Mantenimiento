package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasVendedorUseCase;

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
    public EstadisticasVendedorResponse consultar(Authentication autenticado) {
        return EstadisticasVendedorResponse.desde(estadisticasVendedor.consultar(autenticado.getName()));
    }
}
