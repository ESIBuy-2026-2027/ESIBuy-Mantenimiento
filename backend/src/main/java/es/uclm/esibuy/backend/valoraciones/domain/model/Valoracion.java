package es.uclm.esibuy.backend.valoraciones.domain.model;

import java.time.Instant;

import es.uclm.esibuy.backend.valoraciones.domain.exception.ValoracionInvalidaException;

/** Valoración de un producto con autor visible y fecha de creación. */
public record Valoracion(String id, String productoId, String clienteId, String nombreCliente, int puntuacion,
        String comentario, Instant fechaCreacion) {

    private static final int PUNTUACION_MINIMA = 1;
    private static final int PUNTUACION_MAXIMA = 5;
    private static final int COMENTARIO_MAXIMO = 500;

    /** Constructor compatible con valoraciones reconstruidas en pruebas y documentos previos. */
    public Valoracion(String id, String productoId, String clienteId, int puntuacion, String comentario) {
        this(id, productoId, clienteId, null, puntuacion, comentario, null);
    }

    public Valoracion {
        exigir(productoId, "un producto");
        exigir(clienteId, "un cliente");
        if (nombreCliente != null && nombreCliente.isBlank()) {
            nombreCliente = null;
        }
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

    /** Compatibilidad para reconstrucciones y pruebas que no necesitan metadatos del autor. */
    public static Valoracion nueva(String productoId, String clienteId, int puntuacion, String comentario) {
        return new Valoracion(null, productoId, clienteId, puntuacion, comentario);
    }

    /** Una valoración nueva: todavía sin id y con fecha de creación. */
    public static Valoracion nueva(String productoId, String clienteId, String nombreCliente, int puntuacion,
            String comentario) {
        exigir(nombreCliente, "el nombre del cliente");
        return new Valoracion(null, productoId, clienteId, nombreCliente, puntuacion, comentario, Instant.now());
    }

    private static void exigir(String valor, String descripcion) {
        if (valor == null || valor.isBlank()) {
            throw new ValoracionInvalidaException("La valoración necesita " + descripcion);
        }
    }
}
