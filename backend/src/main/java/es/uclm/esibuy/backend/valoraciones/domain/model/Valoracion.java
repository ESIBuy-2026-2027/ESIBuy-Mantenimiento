package es.uclm.esibuy.backend.valoraciones.domain.model;

import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionInvalidaException;

/**
 * Valoración de un producto por un cliente (HU13): una puntuación de 1 a 5 y un comentario opcional. Los demás
 * módulos se refieren al producto y al cliente solo por su identificador.
 *
 * <p>El comentario se guarda sin espacios en los extremos; si no hay, queda vacío. El id es nulo mientras la
 * valoración no se ha guardado.
 */
public record Valoracion(String id, String productoId, String clienteId, int puntuacion, String comentario) {

    private static final int PUNTUACION_MINIMA = 1;
    private static final int PUNTUACION_MAXIMA = 5;
    private static final int COMENTARIO_MAXIMO = 500;


   public Valoracion {
    if (puntuacion < PUNTUACION_MINIMA || puntuacion > PUNTUACION_MAXIMA) {
        throw new ValoracionInvalidaException(
                "La puntuación debe estar entre " + PUNTUACION_MINIMA + " y " + PUNTUACION_MAXIMA);
    }
    comentario = comentario == null ? "" : comentario.strip();
    if (comentario.length() > COMENTARIO_MAXIMO) {
        throw new ValoracionInvalidaException(
                "El comentario no puede superar " + COMENTARIO_MAXIMO + " caracteres");
    }
}

    /** Una valoración nueva: todavía sin id. */
    public static Valoracion nueva(String productoId, String clienteId, int puntuacion, String comentario) {
        return new Valoracion(null, productoId, clienteId, puntuacion, comentario);
    }
}