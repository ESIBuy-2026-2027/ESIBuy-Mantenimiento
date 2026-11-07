package es.uclm.esibuy.backend.estadisticas.domain.model;

/**
 * Un producto del vendedor tal como está ahora (HU17): su stock, si está disponible y sus valoraciones.
 * {@code valoracionMedia} es null si aún no tiene valoraciones.
 */
public record ProductoInventariado(String productoId, String nombre, int stock, boolean disponible,
        Double valoracionMedia, int numeroValoraciones) {
}
