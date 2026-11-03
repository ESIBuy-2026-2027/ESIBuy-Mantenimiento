package es.uclm.esibuy.backend.productos.application.port.in;

/**
 * Media y número de valoraciones de los productos (HU13): el módulo de valoraciones los calcula con todas las
 * valoraciones de un producto cada vez que se guarda una, y los deja en el producto para que el catálogo (HU7 y HU8)
 * pueda mostrarlos, ordenar y filtrar por ellos. No hay un caso de uso del vendedor ni del catálogo con este propósito.
 */
public interface ValoracionesProductoUseCase {

    void actualizarValoracion(String productoId, double media, int numero);
}
