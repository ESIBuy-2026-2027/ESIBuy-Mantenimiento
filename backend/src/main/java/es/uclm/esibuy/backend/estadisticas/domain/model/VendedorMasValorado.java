package es.uclm.esibuy.backend.estadisticas.domain.model;

/** Media ponderada de las valoraciones de los productos de un vendedor. */
public record VendedorMasValorado(String vendedorId, double media, int numeroValoraciones) {
}
