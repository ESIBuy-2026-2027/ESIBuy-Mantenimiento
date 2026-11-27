package es.uclm.esibuy.backend.estadisticas.domain.model;

/** Resumen de valoraciones de un producto de la plataforma, con el vendedor al que pertenece. */
public record ProductoValorado(String productoId, String vendedorId, String nombre, Double valoracionMedia,
        int numeroValoraciones) {
}
