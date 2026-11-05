package es.uclm.esibuy.backend.categorias.application.port.out;

import java.util.Map;

/**
 * Lo que categorías necesita del módulo de productos: cuántos productos tiene cada categoría, para el
 * listado del administrador (HU39).
 */
public interface ProductosPorCategoriaPort {

    // Número de productos de cada categoría, por su id. Las categorías sin productos no aparecen.
    Map<String, Long> contarPorCategoria();
}
