package es.uclm.esibuy.backend.categorias.application.port.in;

import java.util.List;

/** Puerto de entrada de la gestión de las categorías por el administrador (HU39). */
public interface AdministracionCategoriasUseCase {

    /**
     * Devuelve todas las categorías, también las inactivas, ordenadas por nombre y con su número de
     * productos. Una categoría sin productos tiene 0.
     */
    List<CategoriaConProductos> listarConProductos();
}
