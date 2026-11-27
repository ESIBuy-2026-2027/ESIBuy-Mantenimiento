package es.uclm.esibuy.backend.productos.application.port.in;

/** Datos de un producto necesarios para calcular los rankings de estadísticas globales. */
public record ProductoEstadistica(String productoId, String nombre, String vendedorId, Double valoracionMedia,
        int numeroValoraciones) {
}
