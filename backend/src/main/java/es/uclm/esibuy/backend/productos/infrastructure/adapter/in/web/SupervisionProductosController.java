package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import jakarta.validation.constraints.Min;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import es.uclm.esibuy.backend.productos.application.port.in.SupervisionProductosUseCase;

/**
 * Adaptador de entrada web del administrador para supervisar, en solo lectura, los productos de un vendedor
 * (HU26). /api/admin/** solo lo usa un ADMINISTRADOR (SecurityConfig, T58).
 */
@RestController
@RequestMapping("/api")
public class SupervisionProductosController {

    private final SupervisionProductosUseCase supervisionProductos;

    public SupervisionProductosController(SupervisionProductosUseCase supervisionProductos) {
        this.supervisionProductos = supervisionProductos;
    }

    // Pestaña «Productos» del diálogo del vendedor: mismo formato que GET /api/seller/products.
    // 404 si el id no es de un vendedor; 400 si la página o el tamaño es menor que 1 (@Min).
    // PENDIENTE: GET /admin/sellers/{id}/orders, cuando exista el módulo de pedidos.
    @GetMapping("/admin/sellers/{id}/products")
    public ProductoListadoPageResponse listarProductosDeVendedor(
            @PathVariable String id,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        return ProductoListadoPageResponse.desde(supervisionProductos.listarProductosDeVendedor(id, page - 1, size));
    }
}
