package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.estadisticas.application.port.in.EstadisticasAdministradorUseCase;

/** Adaptador de entrada de las estadísticas globales, accesible solo a administradores. */
@RestController
@RequestMapping("/api/admin/statistics")
public class EstadisticasAdministradorController {

    private final EstadisticasAdministradorUseCase estadisticasAdministrador;

    public EstadisticasAdministradorController(EstadisticasAdministradorUseCase estadisticasAdministrador) {
        this.estadisticasAdministrador = estadisticasAdministrador;
    }

    @GetMapping
    public EstadisticasAdministradorResponse consultar() {
        return EstadisticasAdministradorResponse.desde(estadisticasAdministrador.consultar());
    }
}
