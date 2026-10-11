package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.categorias.application.port.in.ConsultaCategoriasUseCase;

/**
 * Consulta de las categorías: {@code GET /api/categorias}, el contrato que ya usan el registro del vendedor,
 * la edición del vendedor (HU22) y los formularios de productos. Es público (SecurityConfig): el registro
 * del vendedor lo usa sin sesión.
 */
@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {

    private final ConsultaCategoriasUseCase consultaCategorias;

    public CategoriaController(ConsultaCategoriasUseCase consultaCategorias) {
        this.consultaCategorias = consultaCategorias;
    }

    @GetMapping
    public List<CategoriaResponse> listar() {
        return List.of();
    }
}
