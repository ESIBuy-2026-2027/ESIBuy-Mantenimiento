package es.uclm.esibuy.backend.categorias.infrastructure.adapter.in.web;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/**
 * Adaptador de entrada web del administrador para las categorías: {@code GET /api/admin/categorias} (HU39),
 * {@code POST /api/admin/categorias} (HU40) y {@code PUT /api/admin/categorias/{id}} (HU41).
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

    // Crea una categoría con el estado elegido (HU40, T322) y la devuelve como una fila más del listado: una
    // categoría nueva no tiene productos. 400 si los datos no son válidos; 409 si el nombre ya existe.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoriaAdministracionResponse crear(@Valid @RequestBody CrearCategoriaRequest request) {
        Categoria creada = administracionCategorias.crear(request.aComando());
        return CategoriaAdministracionResponse.desde(new CategoriaConProductos(creada, 0));
    }

    // Cambia el nombre y la descripción de una categoría (HU41, T328) y devuelve la fila actualizada, con su
    // número de productos. 400 si los datos no son válidos; 404 si no existe; 409 si el nombre es el de otra.
    @PutMapping("/{id}")
    public CategoriaAdministracionResponse editar(@PathVariable String id,
            @Valid @RequestBody EditarCategoriaRequest request) {
        return CategoriaAdministracionResponse.desde(administracionCategorias.editar(request.aComando(id)));
    }
}
