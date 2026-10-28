package es.uclm.esibuy.backend.productos.application.port.out;

import java.util.Map;
import java.util.Optional;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;

/**
 * Lo que el módulo de productos necesita de la base de datos (patrón Repository). La aplicación
 * no sabe que detrás está MongoDB: la implementación vive en el adaptador de salida.
 */
public interface ProductoRepositoryPort {

    // Devuelve el producto guardado: si era nuevo, con el id que le ha asignado la base de datos.
    Producto guardar(Producto producto);

    Optional<Producto> buscarPorId(String id);

    // Productos de un vendedor (HU32), del más reciente al más antiguo.
    ProductoPage listarPorVendedor(String vendedorId, ListarProductosQuery query);

    // ¿Tiene ya el vendedor un producto con ese nombre? No distingue mayúsculas (HU31).
    boolean existePorVendedorYNombre(String vendedorId, String nombre);

    // Borrado físico del producto (HU35): el documento desaparece de la colección.
    void eliminar(String id);

    ProductoPage buscarEnCatalogo(BuscarCatalogoQuery query);

    // Número de productos de cada categoría, por su id (HU39). Las categorías sin productos no aparecen.
    Map<String, Long> contarPorCategoria();
}
