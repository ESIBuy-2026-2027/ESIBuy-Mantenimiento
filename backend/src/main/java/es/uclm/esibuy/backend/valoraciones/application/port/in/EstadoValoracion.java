package es.uclm.esibuy.backend.valoraciones.application.port.in;

/** Estado de compra y valoración del producto para el cliente autenticado. */
public record EstadoValoracion(boolean haComprado, boolean haValorado, String miValoracionId) {
}
