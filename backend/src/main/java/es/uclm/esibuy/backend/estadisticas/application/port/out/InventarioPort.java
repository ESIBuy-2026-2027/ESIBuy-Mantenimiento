package es.uclm.esibuy.backend.estadisticas.application.port.out;

import java.util.List;

import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoInventariado;

/** Lo que estadísticas necesita del módulo de productos: todos los productos del vendedor. */
public interface InventarioPort {

    List<ProductoInventariado> productosDe(String vendedorId);
}
