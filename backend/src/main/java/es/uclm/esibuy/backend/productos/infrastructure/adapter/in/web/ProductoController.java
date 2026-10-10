package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Min;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;

/**
 * Adaptador de entrada web del módulo de productos. Recibe la petición, la traduce a una consulta y
 * delega en el puerto de entrada. /api/seller/** solo lo usa un VENDEDOR (SecurityConfig, T57).
 */
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ListadoProductosUseCase listadoProductos;

    public ProductoController(ListadoProductosUseCase listadoProductos) {
        this.listadoProductos = listadoProductos;
    }

    // Listado de los productos propios (HU32): el vendedor sale de la sesión, nunca de un parámetro.
    // Una página o un tamaño menor que 1 los rechaza Spring con 400 (validación de @Min).
    @GetMapping("/seller/products")
    public ProductoListadoPageResponse listarMisProductos(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            Authentication autenticado) {
        return ProductoListadoPageResponse.desde(listadoProductos.listarMisProductos(
                autenticado.getName(), new ListarProductosQuery(search, category, page - 1, size)));
    }
}
