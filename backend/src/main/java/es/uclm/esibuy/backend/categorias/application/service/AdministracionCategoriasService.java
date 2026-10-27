package es.uclm.esibuy.backend.categorias.application.service;

import java.util.List;
import java.util.Map;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;

/**
 * Implementa la gestión de categorías del administrador. Solo habla con el exterior a través del
 * repositorio de categorías y del puerto hacia el módulo de productos.
 */
public class AdministracionCategoriasService implements AdministracionCategoriasUseCase {

    private final CategoriaRepositoryPort categoriaRepository;
    private final ProductosPorCategoriaPort productosPorCategoria;

    public AdministracionCategoriasService(CategoriaRepositoryPort categoriaRepository,
            ProductosPorCategoriaPort productosPorCategoria) {
        this.categoriaRepository = categoriaRepository;
        this.productosPorCategoria = productosPorCategoria;
    }

    // El repositorio ya las da ordenadas por nombre; las que no salen en el recuento no tienen productos.
    @Override
    public List<CategoriaConProductos> listarConProductos() {
        Map<String, Long> productos = productosPorCategoria.contarPorCategoria();
        return categoriaRepository.listar().stream()
                .map(categoria -> new CategoriaConProductos(categoria,
                        productos.getOrDefault(categoria.id(), 0L)))
                .toList();
    }
}
