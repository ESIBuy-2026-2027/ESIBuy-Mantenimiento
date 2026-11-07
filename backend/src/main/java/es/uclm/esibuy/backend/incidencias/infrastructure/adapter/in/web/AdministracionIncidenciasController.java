package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;

/**
 * Adaptador de entrada web de la supervisión de incidencias (HU49). Solo lo usa el administrador: lo limitan
 * {@code /api/admin/**} en {@code SecurityConfig} y el {@code @PreAuthorize} del servicio.
 */
@RestController
@RequestMapping("/api/admin/incidencias")
public class AdministracionIncidenciasController {

    private final SupervisionIncidenciasUseCase supervisionIncidencias;

    public AdministracionIncidenciasController(SupervisionIncidenciasUseCase supervisionIncidencias) {
        this.supervisionIncidencias = supervisionIncidencias;
    }

    // Sin Authentication: el administrador ve todas las incidencias, no hay nada de la sesión que pasar.
    // La API cuenta las páginas desde 1 y el dominio desde 0.
    @GetMapping
    public IncidenciaSupervisadaPageResponse consultar(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return IncidenciaSupervisadaPageResponse.desde(supervisionIncidencias.consultarTodas(page - 1, size));
    }
}
