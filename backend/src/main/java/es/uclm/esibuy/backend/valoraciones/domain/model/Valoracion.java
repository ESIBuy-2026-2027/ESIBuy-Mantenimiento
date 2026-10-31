package es.uclm.esibuy.backend.valoraciones.domain.model;

/**
 * Valoración de un producto por un cliente (HU13): una puntuación y un comentario opcional. Los demás módulos se
 * refieren al producto y al cliente solo por su identificador.
 *
 * <p>El comentario se guarda sin espacios en los extremos; si no hay, queda vacío. El id es nulo mientras la
 * valoración no se ha guardado.
 */
public record Valoracion(String id, String productoId, String clienteId, int puntuacion, String comentario) {

    public Valoracion {
        comentario = comentario == null ? "" : comentario.strip();
    }

    /** Una valoración nueva: todavía sin id. */
    public static Valoracion nueva(String productoId, String clienteId, int puntuacion, String comentario) {
        return new Valoracion(null, productoId, clienteId, puntuacion, comentario);
    }
}