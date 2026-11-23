package es.uclm.esibuy.backend.productos.application.port.in;

import java.math.BigDecimal;

/**
 * Criterios del listado de productos de un vendedor (HU32), independientes de HTTP y MongoDB.
 *
 * @param busqueda texto que se busca en el nombre, sin distinguir mayúsculas; null o vacío = sin filtro
 * @param categoriaId filtro opcional por categoría
 * @param disponible filtro opcional por disponibilidad
 * @param criterioOrden campo opcional por el que ordenar
 * @param direccionOrden sentido opcional de la ordenación
 * @param precioMinimo precio mínimo opcional
 * @param precioMaximo precio máximo opcional
 * @param pagina índice de página, empezando en cero
 * @param tamano número máximo de productos por página
 */
public record ListarProductosQuery(
        String busqueda,
        String categoriaId,
        Boolean disponible,
        CriterioOrdenProductos criterioOrden,
        DireccionOrden direccionOrden,
        BigDecimal precioMinimo,
        BigDecimal precioMaximo,
        int pagina,
        int tamano
) {
    public ListarProductosQuery(String busqueda, String categoriaId, int pagina, int tamano) {
        this(busqueda, categoriaId, null, null, null, null, null, pagina, tamano);
    }
}
