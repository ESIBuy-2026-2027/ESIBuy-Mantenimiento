package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

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

    @GetMapping("/products")
    public ProductoCatalogoPageResponse buscarProductos(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer minRating,
            @RequestParam(defaultValue = "nombre") String sort,
            @RequestParam(defaultValue = "asc") String order) {
        CriterioOrdenCatalogo criterio = "valoracion".equals(sort)
                ? CriterioOrdenCatalogo.VALORACION
                : CriterioOrdenCatalogo.NOMBRE;
        return ProductoCatalogoPageResponse.desde(catalogoProductos.buscarProductos(new BuscarCatalogoQuery(
                search, category, minRating, criterio, "desc".equals(order), page - 1, size)));
    }
}