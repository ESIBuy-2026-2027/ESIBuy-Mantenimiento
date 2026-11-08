package es.uclm.esibuy.backend.productos.application.port.in;

/**
 * Lo que otros módulos ven de un producto del vendedor para sus estadísticas (HU17): su stock, si está disponible y
 * sus valoraciones, sin ver el dominio de productos. {@code valoracionMedia} es null si aún no tiene valoraciones.
 */
public record ProductoInventario(String id, String nombre, int stock, boolean disponible, Double valoracionMedia,
        int numeroValoraciones) {
}
