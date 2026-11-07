package es.uclm.esibuy.backend.incidencias.domain.model;

import java.util.List;

/**
 * Una página de incidencias: la página (la primera es la 0), su tamaño y el total de incidencias de todas las
 * páginas.
 */
public record IncidenciaPage(List<Incidencia> incidencias, int pagina, int tamano, long totalElementos) {
}