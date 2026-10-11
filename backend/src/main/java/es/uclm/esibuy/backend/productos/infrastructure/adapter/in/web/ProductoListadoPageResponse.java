package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

public record ProductoListadoPageResponse(
        List<ProductoListadoResponse> items,
        int page,
        int size,
        long totalItems,
        long totalPages
) {

    // page empieza en 1 para el frontend; totalPages es 0 si no hay resultados.
    static ProductoListadoPageResponse desde(ProductoPage pagina) {
        long totalPages = (pagina.totalElementos() + pagina.tamano() - 1) / pagina.tamano();
        return new ProductoListadoPageResponse(
                pagina.productos().stream().map(ProductoListadoResponse::desde).toList(),
                pagina.pagina() + 1,
                pagina.tamano(),
                pagina.totalElementos(),
                totalPages
        );
    }
}
