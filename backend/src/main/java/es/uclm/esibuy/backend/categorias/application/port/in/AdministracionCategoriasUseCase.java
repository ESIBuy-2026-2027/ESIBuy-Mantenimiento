package es.uclm.esibuy.backend.categorias.application.port.in;

import java.util.List;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

/** Puerto de entrada de la gestión de las categorías por el administrador (HU39 y HU40). */
public interface AdministracionCategoriasUseCase {

    /**
     * Devuelve todas las categorías, también las inactivas, ordenadas por nombre y con su número de
     * productos. Una categoría sin productos tiene 0.
     */
    List<CategoriaConProductos> listarConProductos();

    /**
     * Crea una categoría con el estado elegido y la devuelve con su id. El nombre no puede repetirse sin
     * distinguir mayúsculas ni tildes ({@code CategoriaDuplicadaException}); los datos se validan en el dominio
     * ({@code CategoriaInvalidaException}).
     */
    Categoria crear(CrearCategoriaCommand comando);

    /**
     * Cambia el nombre y la descripción de una categoría y la devuelve con su número de productos; el estado
     * no se toca. La categoría debe existir ({@code CategoriaNoEncontradaException}), el nombre no puede ser el de
     * otra categoría sin distinguir mayúsculas ni tildes ({@code CategoriaDuplicadaException}) y los datos se
     * validan en el dominio ({@code CategoriaInvalidaException}).
     */
    CategoriaConProductos editar(EditarCategoriaCommand comando);
}
