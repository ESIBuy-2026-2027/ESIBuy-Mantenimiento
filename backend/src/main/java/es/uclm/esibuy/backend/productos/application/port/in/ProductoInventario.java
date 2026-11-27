package es.uclm.esibuy.backend.productos.application.port.in;

/**
 * Lo que otros módulos ven de un producto del vendedor para sus estadísticas (HU17): su imagen, stock,
 * disponibilidad y valoraciones, sin ver el dominio de productos. {@code valoracionMedia} es null si aún no tiene
 * valoraciones.
 */
public record ProductoInventario(String id, String nombre, String imagen, int stock, boolean disponible,
        Double valoracionMedia, int numeroValoraciones) {
}
