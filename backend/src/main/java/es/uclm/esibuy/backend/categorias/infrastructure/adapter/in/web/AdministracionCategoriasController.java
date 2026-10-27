package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;

/**
 * Adaptador de entrada web del administrador para las categorías (HU39): {@code GET /api/admin/categorias}.
 * /api/admin/** solo lo usa un ADMINISTRADOR (SecurityConfig, T58): sin sesión 401, con otro rol 403.
 */
@RestController
@RequestMapping("/api/admin/categorias")
public class AdministracionCategoriasController {

    private final AdministracionCategoriasUseCase administracionCategorias;

    public AdministracionCategoriasController(AdministracionCategoriasUseCase administracionCategorias) {
        this.administracionCategorias = administracionCategorias;
    }

    // Todas las categorías, también las inactivas, ordenadas por nombre y con su número de productos.
    @GetMapping
    public List<CategoriaAdministracionResponse> listar() {
        return administracionCategorias.listarConProductos().stream()
                .map(CategoriaAdministracionResponse::desde)
                .toList();
    }
}
