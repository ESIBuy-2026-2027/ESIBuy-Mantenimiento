package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.usuarios.application.port.in.GestionPerfilUseCase;

/**
 * Adaptador de entrada web del perfil propio (HU4): {@code GET /api/perfil}. Solo lo usan un CLIENTE o un
 * VENDEDOR (SecurityConfig): sin sesión 401, el administrador 403. El usuario sale de la sesión (su email),
 * nunca de la petición, así que nadie puede pedir el perfil de otro.
 */
@RestController
@RequestMapping("/api/perfil")
public class PerfilController {

    private final GestionPerfilUseCase gestionPerfil;

    public PerfilController(GestionPerfilUseCase gestionPerfil) {
        this.gestionPerfil = gestionPerfil;
    }

    @GetMapping
    public PerfilResponse consultar(Authentication autenticado) {
        return PerfilResponse.desde(gestionPerfil.consultarPerfil(autenticado.getName()));
    }
}
