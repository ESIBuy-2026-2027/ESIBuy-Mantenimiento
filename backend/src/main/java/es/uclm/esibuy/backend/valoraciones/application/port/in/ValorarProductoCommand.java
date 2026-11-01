package es.uclm.esibuy.backend.valoraciones.application.port.in;

/** Lo que el cliente envía al valorar un producto: la puntuación de 1 a 5 y un comentario opcional. */
public record ValorarProductoCommand(String emailCliente, String productoId, int puntuacion, String comentario) {
}