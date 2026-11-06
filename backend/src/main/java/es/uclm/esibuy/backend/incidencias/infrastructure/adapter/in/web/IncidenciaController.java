package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;

/** Esqueleto del ciclo rojo: responde 201 pero todavía no crea nada. */
@RestController
@RequestMapping("/api/perfil/incidencias")
public class IncidenciaController {

    private final CreacionIncidenciasUseCase creacionIncidencias;

    public IncidenciaController(CreacionIncidenciasUseCase creacionIncidencias) {
        this.creacionIncidencias = creacionIncidencias;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public IncidenciaResponse crear(@RequestBody CrearIncidenciaRequest request, Authentication autenticado) {
        return null;
    }
}