package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaPage;

/**
 * Una página de incidencias, con el mismo formato que los demás listados de la API: la página empieza en 1 y vienen
 * el total de incidencias y el de páginas.
 */
public record IncidenciaPageResponse(List<IncidenciaResponse> items, int page, int size, long totalItems,
        long totalPages) {

    public static IncidenciaPageResponse desde(IncidenciaPage pagina) {
        long totalPages = (pagina.totalElementos() + pagina.tamano() - 1) / pagina.tamano();
        return new IncidenciaPageResponse(
                pagina.incidencias().stream().map(IncidenciaResponse::desde).toList(),
                pagina.pagina() + 1,
                pagina.tamano(),
                pagina.totalElementos(),
                totalPages);
    }
}