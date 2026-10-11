package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Min;
import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;


import es.uclm.esibuy.backend.productos.application.port.in.ListadoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;

import es.uclm.esibuy.backend.productos.application.port.in.GestionProductosUseCase;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


/**
 * Adaptador de entrada web del módulo de productos. Recibe la petición, la traduce a una consulta y
 * delega en el puerto de entrada. /api/seller/** solo lo usa un VENDEDOR (SecurityConfig, T57).
 */
@RestController
@RequestMapping("/api")
public class ProductoController {

    private final ListadoProductosUseCase listadoProductos;
    private final GestionProductosUseCase gestionProductos;

    public ProductoController(ListadoProductosUseCase listadoProductos, GestionProductosUseCase gestionProductos) {
        this.listadoProductos = listadoProductos;
        this.gestionProductos = gestionProductos;
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

    @PostMapping("/seller/products")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductoResponse crearProducto(@Valid @RequestBody ProductoRequest request, Authentication autenticado) {
        return ProductoResponse.desde(gestionProductos.crear(request.aComando(autenticado.getName())));
    }
    
}
