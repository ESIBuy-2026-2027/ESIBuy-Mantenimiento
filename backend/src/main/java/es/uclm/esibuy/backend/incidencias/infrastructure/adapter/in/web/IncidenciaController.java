package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.incidencias.application.port.in.CreacionIncidenciasUseCase;

/**
 * Adaptador de entrada web de las incidencias (HU44). Recibe la petición, delega en el puerto de entrada y responde
 * con la incidencia creada. El autor sale siempre de la sesión. Cliente y vendedor la usan desde su perfil
 * (/api/perfil/** en SecurityConfig).
 */
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
        return IncidenciaResponse.desde(creacionIncidencias.crear(request.aComando(autenticado.getName())));
    }
}