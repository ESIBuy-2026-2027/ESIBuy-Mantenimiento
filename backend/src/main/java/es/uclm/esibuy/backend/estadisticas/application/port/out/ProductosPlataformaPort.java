package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoValorado;

/** Datos de todos los productos que necesita el módulo de estadísticas. */
public interface ProductosPlataformaPort {

    List<ProductoValorado> consultar();
}
