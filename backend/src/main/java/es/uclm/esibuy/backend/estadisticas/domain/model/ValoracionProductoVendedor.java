package es.uclm.esibuy.backend.estadisticas.domain.model;

/** Un producto valorado del inventario del vendedor, con su imagen y los datos de valoración. */
public record ValoracionProductoVendedor(String productoId, String nombre, String imagen, double media, int numero) {
}
