package es.uclm.esibuy.backend.productos.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.productos.application.port.in.PaginaCatalogo;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

public record ProductoCatalogoPageResponse(
        List<ProductoCatalogoResponse> items,
        int page,
        int size,
        long totalItems,
        long totalPages
) {

    // page empieza en 1 para el frontend; totalPages es 0 si no hay resultados.
    static ProductoCatalogoPageResponse desde(PaginaCatalogo catalogo) {
        ProductoPage pagina = catalogo.pagina();
        long totalPages = (pagina.totalElementos() + pagina.tamano() - 1) / pagina.tamano();
        return new ProductoCatalogoPageResponse(
                pagina.productos().stream()
                        .map(producto -> ProductoCatalogoResponse.desde(producto, catalogo.clientePremium()))
                        .toList(),
                pagina.pagina() + 1,
                pagina.tamano(),
                pagina.totalElementos(),
                totalPages
        );
    }
}
