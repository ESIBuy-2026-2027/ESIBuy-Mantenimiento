package es.uclm.esibuy.backend.estadisticas.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record EvolucionVentas(LocalDate fecha, int pedidos, int unidades, BigDecimal ingresos) {
}
