package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

public enum PeriodoEstadisticasVendedor {
    TODO_EL_PERIODO;

    public Optional<Instant> desde(LocalDate hoy, ZoneId zona) {
        return Optional.empty();
    }

    public GranularidadVentas granularidad() {
        return GranularidadVentas.DIA;
    }
}
