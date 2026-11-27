package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;

/** Ventas globales que necesita el módulo de estadísticas para el ranking de productos. */
public interface VentasPlataformaPort {

    List<VentaRegistrada> consultar();
}
