package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.util.List;

public record ResumenValoracionesPeriodo(Double media, int total, List<ValoracionProductoPeriodo> productos,
        List<PuntuacionValoracion> distribucion) {
}
