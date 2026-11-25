package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;

/**
 * Adaptador de entrada web del catálogo de productos (HU7), sus destacados públicos
 * para la portada y el detalle de cada producto (HU8).
 */
@RestController
@RequestMapping("/api")
public class CatalogoController {

    private final CatalogoProductosUseCase catalogoProductos;

    public CatalogoController(CatalogoProductosUseCase catalogoProductos) {
        this.catalogoProductos = catalogoProductos;
    }

    /** Consulta pública y de solo lectura de los productos mejor valorados de la portada. */
    @GetMapping("/products/featured")
    public List<ProductoDestacadoResponse> consultarDestacados(
            @RequestParam(defaultValue = "10") @Min(1) @Max(10) int limit) {
        return catalogoProductos.buscarDestacados(limit).stream()
                .map(ProductoDestacadoResponse::desde)
                .toList();
    }

    @GetMapping("/products")
    public ProductoCatalogoPageResponse buscarProductos(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) @Min(1) @Max(5) Integer minRating,
            @RequestParam(defaultValue = "false") boolean inStock,
            @RequestParam(defaultValue = "false") boolean offersOnly,
            @RequestParam(defaultValue = "nombre") @Pattern(regexp = "nombre|valoracion") String sort,
            @RequestParam(defaultValue = "asc") @Pattern(regexp = "asc|desc") String order,
            Authentication autenticado) {
        CriterioOrdenCatalogo criterio = "valoracion".equals(sort)
                ? CriterioOrdenCatalogo.VALORACION
                : CriterioOrdenCatalogo.NOMBRE;
        BuscarCatalogoQuery query = new BuscarCatalogoQuery(
                search, category, minRating, inStock, offersOnly, false, criterio,
                "desc".equals(order), page - 1, size);
        return ProductoCatalogoPageResponse.desde(catalogoProductos.buscarProductos(query, autenticado.getName()));
    }

    @GetMapping("/products/{id}")
    public DetalleProductoCatalogoResponse consultarDetalle(@PathVariable String id, Authentication autenticado) {
        return DetalleProductoCatalogoResponse.desde(catalogoProductos.consultarDetalle(id, autenticado.getName()));
    }
}
