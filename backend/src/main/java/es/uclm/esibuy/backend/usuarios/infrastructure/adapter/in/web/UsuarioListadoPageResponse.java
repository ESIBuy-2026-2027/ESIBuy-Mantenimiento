package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.usuarios.application.port.in.UsuarioResumenPage;

public record UsuarioListadoPageResponse(
        List<UsuarioListadoResponse> items,
        int page,
        int size,
        long totalItems,
        long totalPages
) {

    static UsuarioListadoPageResponse desde(UsuarioResumenPage pagina) {
        long totalPages = (pagina.totalElementos() + pagina.tamano() - 1) / pagina.tamano();
        return new UsuarioListadoPageResponse(
                pagina.usuarios().stream().map(UsuarioListadoResponse::desde).toList(),
                pagina.pagina() + 1,
                pagina.tamano(),
                pagina.totalElementos(),
                totalPages
        );
    }
}
