package es.uclm.esibuy.backend.categorias.application.service;

import java.util.List;
import java.util.Map;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;

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

    // Primero el dominio valida los datos; después se comprueba el nombre ya sin espacios en los extremos. El
    // índice único de la base de datos cubre dos altas simultáneas que pasen a la vez esta comprobación.
    @Override
    public Categoria crear(CrearCategoriaCommand comando) {
        Categoria categoria = new Categoria(null, comando.nombre(), comando.descripcion(), comando.estado());
        if (categoriaRepository.existePorNombre(categoria.nombre())) {
            throw new CategoriaDuplicadaException();
        }
        return categoriaRepository.guardar(categoria);
    }
}
