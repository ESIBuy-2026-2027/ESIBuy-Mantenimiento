package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.in.web;

import java.util.List;

import es.uclm.esibuy.backend.incidencias.domain.model.IncidenciaSupervisadaPage;

/**
 * Una página de incidencias supervisadas tal como la devuelve la API al administrador (HU49). Como
 * {@code IncidenciaPageResponse}: la página empieza en 1 para el frontend, aunque el dominio la cuente desde 0.
 */
public record IncidenciaSupervisadaPageResponse(List<IncidenciaSupervisadaResponse> items, int page, int size,
        long totalItems, int totalPages) {

    public static IncidenciaSupervisadaPageResponse desde(IncidenciaSupervisadaPage pagina) {
        List<IncidenciaSupervisadaResponse> items = pagina.incidencias().stream()
                .map(IncidenciaSupervisadaResponse::desde).toList();
        int totalPages = (int) Math.ceil((double) pagina.totalElementos() / pagina.tamano());
        return new IncidenciaSupervisadaPageResponse(items, pagina.pagina() + 1, pagina.tamano(),
                pagina.totalElementos(), totalPages);
    }
}
