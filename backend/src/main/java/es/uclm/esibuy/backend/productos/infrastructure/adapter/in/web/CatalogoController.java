package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;

/**
 * Adaptador de entrada web del catálogo de productos del cliente (HU7). Recibe la petición, la traduce a una
 * consulta y delega en el puerto de entrada.
 */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoProductosUseCase catalogoProductos;

    public CatalogoController(CatalogoProductosUseCase catalogoProductos) {
        this.catalogoProductos = catalogoProductos;
    }

    // Catálogo del cliente (HU7): la página llega empezando en 1 y el caso de uso la recibe empezando en 0.
    // Un parámetro fuera de rango o un orden desconocido los rechaza Spring con 400 (validación de las anotaciones).
    @GetMapping("/products")
    public ProductoCatalogoPageResponse buscarProductos(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @Min(1) @Max(5) Integer minRating,
            @RequestParam(defaultValue = "nombre") @Pattern(regexp = "nombre|valoracion") String sort,
            @RequestParam(defaultValue = "asc") @Pattern(regexp = "asc|desc") String order) {
        CriterioOrdenCatalogo criterio = "valoracion".equals(sort)
                ? CriterioOrdenCatalogo.VALORACION
                : CriterioOrdenCatalogo.NOMBRE;
        return ProductoCatalogoPageResponse.desde(catalogoProductos.buscarProductos(new BuscarCatalogoQuery(
                search, category, minRating, criterio, "desc".equals(order), page - 1, size)));
    }
}
