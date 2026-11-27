package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.time.Instant;
import java.util.Optional;

import es.uclm.esibuy.backend.estadisticas.domain.model.ResumenValoracionesPeriodo;

public interface ValoracionesPort {

    ResumenValoracionesPeriodo resumenDe(String emailVendedor, Optional<Instant> desde, Instant hasta);
}
