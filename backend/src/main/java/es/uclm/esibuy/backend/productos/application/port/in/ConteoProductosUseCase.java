package es.uclm.esibuy.backend.productos.application.port.in;

import java.util.Map;

/**
 * Puerto de entrada del recuento de productos por categoría. Lo usa el módulo de categorías para el
 * listado del administrador (HU39), sin conocer el dominio de productos.
 */
public interface ConteoProductosUseCase {

    /** Número de productos de cada categoría, por su id. Las categorías sin productos no aparecen. */
    Map<String, Long> contarPorCategoria();
}
