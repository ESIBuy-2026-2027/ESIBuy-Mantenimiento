package es.uclm.esibuy.backend.incidencias.domain.model;

import java.util.List;

/** Una página de incidencias supervisadas (HU49): como {@link IncidenciaPage}, pero cada una con su autor. */
public record IncidenciaSupervisadaPage(List<IncidenciaSupervisada> incidencias, int pagina, int tamano,
        long totalElementos) {
}
