package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.incidencias.application.port.in.ResolucionIncidenciasUseCase;
import es.uclm.esibuy.backend.incidencias.application.port.in.SupervisionIncidenciasUseCase;

/**
 * Adaptador de entrada web de la supervisión (HU49) y la resolución (HU50) de incidencias. Solo lo usa el
 * administrador: lo limitan
 * {@code /api/admin/**} en {@code SecurityConfig} y el {@code @PreAuthorize} del servicio.
 */
@RestController
@RequestMapping("/api/admin/incidencias")
public class AdministracionIncidenciasController {

    private final SupervisionIncidenciasUseCase supervisionIncidencias;
    private final ResolucionIncidenciasUseCase resolucionIncidencias;

    public AdministracionIncidenciasController(SupervisionIncidenciasUseCase supervisionIncidencias,
            ResolucionIncidenciasUseCase resolucionIncidencias) {
        this.supervisionIncidencias = supervisionIncidencias;
        this.resolucionIncidencias = resolucionIncidencias;
    }

    // Sin Authentication: el administrador ve todas las incidencias, no hay nada de la sesión que pasar.
    // La API cuenta las páginas desde 1 y el dominio desde 0.
    @GetMapping
    public IncidenciaSupervisadaPageResponse consultar(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return IncidenciaSupervisadaPageResponse.desde(supervisionIncidencias.consultarTodas(page - 1, size));
    }

    // Abre una incidencia pendiente con la respuesta del administrador (HU50, T300) y la devuelve. 400 si la
    // respuesta falta o es demasiado larga; 404 si no existe; 409 si ya está abierta.
    @PatchMapping("/{id}/abrir")
    public IncidenciaResponse abrir(@PathVariable String id, @RequestBody AbrirIncidenciaRequest request) {
        return IncidenciaResponse.desde(resolucionIncidencias.abrir(id, request.respuesta()));
    }
}
