package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.in.web;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;

/**
 * Adaptador de entrada web de las preferencias del cliente (HU46): {@code GET} y {@code PUT}
 * {@code /api/customer/preferences}, y {@code POST} y {@code DELETE} {@code /api/customer/preferences/categories} para
 * marcar y desmarcar cada categoría favorita (AB#528). Recibe la petición y delega en el puerto de entrada. El cliente
 * sale siempre de la sesión, nunca de la petición. /api/customer/** solo lo usa un CLIENTE (SecurityConfig): sin sesión
 * 401, con otro rol o sin token CSRF 403.
 */
@RestController
@RequestMapping("/api/customer/preferences")
public class PreferenciasController {

    private final GestionPreferenciasUseCase gestionPreferencias;

    public PreferenciasController(GestionPreferenciasUseCase gestionPreferencias) {
        this.gestionPreferencias = gestionPreferencias;
    }

    @GetMapping
    public PreferenciasResponse consultar(Authentication autenticado) {
        return new PreferenciasResponse(gestionPreferencias.consultar(autenticado.getName()));
    }

    // 400 si faltan las categorías, el cuerpo no se puede leer o alguna categoría no existe o no está activa
    @PutMapping
    public PreferenciasResponse guardar(@Valid @RequestBody GuardarPreferenciasRequest request,
            Authentication autenticado) {
        return new PreferenciasResponse(gestionPreferencias.guardar(autenticado.getName(), request.categorias()));
    }

    // 400 si falta la categoría o el cuerpo no se puede leer; 404 si la categoría no existe o no está activa
    @PostMapping("/categories")
    public PreferenciasResponse marcar(@Valid @RequestBody MarcarCategoriaRequest request, Authentication autenticado) {
        return new PreferenciasResponse(gestionPreferencias.marcar(autenticado.getName(), request.categoriaId()));
    }

    // Desmarcar una categoría que no es favorita no es un error: responde igual, con las favoritas
    @DeleteMapping("/categories/{categoriaId}")
    public PreferenciasResponse desmarcar(@PathVariable String categoriaId, Authentication autenticado) {
        return new PreferenciasResponse(gestionPreferencias.desmarcar(autenticado.getName(), categoriaId));
    }
}
