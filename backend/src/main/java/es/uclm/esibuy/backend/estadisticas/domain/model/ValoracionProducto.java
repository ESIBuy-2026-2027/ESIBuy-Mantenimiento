package es.uclm.esibuy.backend.estadisticas.domain.model;

/** La media y el número de valoraciones de un producto (HU17). */
public record ValoracionProducto(String productoId, String nombre, double media, int numero) {
}
