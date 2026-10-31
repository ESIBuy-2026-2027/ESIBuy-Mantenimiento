package es.uclm.esibuy.backend.categorias.application.service;

import java.util.List;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;

import es.uclm.esibuy.backend.categorias.application.port.in.AdministracionCategoriasUseCase;
import es.uclm.esibuy.backend.categorias.application.port.in.CategoriaConProductos;
import es.uclm.esibuy.backend.categorias.application.port.out.CategoriaRepositoryPort;
import es.uclm.esibuy.backend.categorias.application.port.in.CrearCategoriaCommand;
import es.uclm.esibuy.backend.categorias.application.port.out.ProductosPorCategoriaPort;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaDuplicadaException;
import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.shared.seguridad.Permisos;
import es.uclm.esibuy.backend.categorias.application.port.in.EditarCategoriaCommand;
import es.uclm.esibuy.backend.categorias.domain.exception.CategoriaNoEncontradaException;

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
    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
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
    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public Categoria crear(CrearCategoriaCommand comando) {
        Categoria categoria = new Categoria(null, comando.nombre(), comando.descripcion(), comando.estado());
        if (categoriaRepository.existePorNombre(categoria.nombre())) {
            throw new CategoriaDuplicadaException();
        }
        return categoriaRepository.guardar(categoria);
    }
    
    // Primero se comprueba que existe; después el dominio valida los datos y, con el nombre ya sin espacios en
    // los extremos, se comprueba que no es el de otra categoría (el suyo propio sí vale). El estado se conserva.
    // El índice único cubre dos cambios simultáneos al mismo nombre.
    @PreAuthorize(Permisos.SOLO_ADMINISTRADOR)
    @Override
    public CategoriaConProductos editar(EditarCategoriaCommand comando) {
        Categoria actual = categoriaRepository.buscarPorId(comando.id())
                .orElseThrow(CategoriaNoEncontradaException::new);
        Categoria editada = new Categoria(actual.id(), comando.nombre(), comando.descripcion(), actual.estado());
        if (categoriaRepository.existeOtraConNombre(editada.nombre(), actual.id())) {
            throw new CategoriaDuplicadaException();
        }
        Categoria guardada = categoriaRepository.guardar(editada);
        return new CategoriaConProductos(guardada,
                productosPorCategoria.contarPorCategoria().getOrDefault(guardada.id(), 0L));
    }
}
