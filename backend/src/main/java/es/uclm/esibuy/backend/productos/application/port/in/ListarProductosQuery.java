package es.uclm.esibuy.backend.productos.application.port.in;

/**
 * Criterios del listado de productos de un vendedor (HU32), independientes de HTTP y MongoDB.
 *
 * @param busqueda texto que se busca en el nombre, sin distinguir mayúsculas; null o vacío = sin filtro
 * @param categoriaId filtro opcional por categoría
 * @param pagina índice de página, empezando en cero
 * @param tamano número máximo de productos por página
 */
public record ListarProductosQuery(
        String busqueda,
        String categoriaId,
        int pagina,
        int tamano
) {
}
