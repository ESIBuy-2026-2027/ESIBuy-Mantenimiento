package es.uclm.esibuy.backend.incidencias.domain.model;

import java.time.Instant;

import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;

/**
 * Incidencia que un cliente o un vendedor reporta desde su perfil (HU44) para que un administrador la revise y le
 * responda. Los demás módulos se refieren al autor solo por su identificador.
 *
 * <p>El asunto (hasta 100 caracteres) y la descripción (hasta 1000) son obligatorios; los tres textos se guardan
 * sin espacios en los extremos y la respuesta, que escribe un administrador, queda vacía si no hay. El id es nulo
 * mientras la incidencia no se ha guardado.
 */
public record Incidencia(String id, String autorId, String asunto, String descripcion, EstadoIncidencia estado,
        String respuesta, Instant fechaCreacion) {

    private static final int ASUNTO_MAXIMO = 100;
    private static final int DESCRIPCION_MAXIMA = 1000;

    public Incidencia {
        exigir(autorId, "un autor");
        asunto = exigirTexto(asunto, "un asunto", ASUNTO_MAXIMO);
        descripcion = exigirTexto(descripcion, "una descripción", DESCRIPCION_MAXIMA);
        if (estado == null) {
            throw new IncidenciaInvalidaException("La incidencia necesita un estado");
        }
        respuesta = respuesta == null ? "" : respuesta.strip();
        if (fechaCreacion == null) {
            throw new IncidenciaInvalidaException("La incidencia necesita una fecha de creación");
        }
    }

    /** Una incidencia nueva: todavía sin id, pendiente de revisar y sin respuesta. */
    public static Incidencia nueva(String autorId, String asunto, String descripcion, Instant fechaCreacion) {
        return new Incidencia(null, autorId, asunto, descripcion, EstadoIncidencia.PENDIENTE, "", fechaCreacion);
    }

    public Incidencia abrir(String respuesta) {
        throw new UnsupportedOperationException("Pendiente de implementar (HU50)");
    }

    private static void exigir(String valor, String descripcion) {
        if (valor == null || valor.isBlank()) {
            throw new IncidenciaInvalidaException("La incidencia necesita " + descripcion);
        }
    }

    // El texto sin espacios en los extremos; el máximo se cuenta ya recortado
    private static String exigirTexto(String valor, String descripcion, int maximo) {
        exigir(valor, descripcion);
        String recortado = valor.strip();
        if (recortado.length() > maximo) {
            throw new IncidenciaInvalidaException(
                    "La incidencia no puede superar " + maximo + " caracteres en " + descripcion.substring(3));
        }
        return recortado;
    }
}