package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.usuarios.domain.model.UsuarioPage;

public record VendedorPageResponse(
        List<VendedorResponse> items,
        int page,
        int size,
        long totalItems,
        long totalPages
) {

    static VendedorPageResponse desde(UsuarioPage pagina) {
        long totalPages = (pagina.totalElementos() + pagina.tamano() - 1) / pagina.tamano();
        return new VendedorPageResponse(
                pagina.usuarios().stream().map(VendedorResponse::desde).toList(),
                pagina.pagina() + 1,
                pagina.tamano(),
                pagina.totalElementos(),
                totalPages
        );
    }
}
