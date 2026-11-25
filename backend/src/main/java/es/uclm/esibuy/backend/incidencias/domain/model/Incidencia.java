package es.uclm.esibuy.backend.incidencias.domain.model;

import java.time.Instant;

import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaInvalidaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaNoAbiertaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaAbiertaException;
import es.uclm.esibuy.backend.incidencias.domain.exception.IncidenciaYaResueltaException;

/**
 * Incidencia que un cliente o un vendedor reporta desde su perfil (HU44) para que un administrador la revise y le
 * responda. Los demás módulos se refieren al autor solo por su identificador.
 *
 * <p>El asunto (hasta 100 caracteres) y la descripción (hasta 1000) son obligatorios; los tres textos se guardan
 * sin espacios en los extremos y la respuesta, que escribe un administrador, queda vacía si no hay. El id es nulo
 * mientras la incidencia no se ha guardado y la fecha de resolución solo existe si está resuelta (HU192).
 */
public record Incidencia(String id, String autorId, String asunto, String descripcion, EstadoIncidencia estado,
        String respuesta, Instant fechaCreacion, Instant fechaResolucion) {

    private static final int ASUNTO_MAXIMO = 100;
    private static final int DESCRIPCION_MAXIMA = 1000;
    private static final int RESPUESTA_MAXIMA = 1000;

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
        if ((estado == EstadoIncidencia.RESUELTA) != (fechaResolucion != null)) {
            throw new IncidenciaInvalidaException(
                    "La incidencia tiene fecha de resolución si, y solo si, está resuelta");
        }
    }

    /** Una incidencia nueva: todavía sin id, pendiente de revisar y sin respuesta. */
    public static Incidencia nueva(String autorId, String asunto, String descripcion, Instant fechaCreacion) {
        return new Incidencia(null, autorId, asunto, descripcion, EstadoIncidencia.PENDIENTE, "", fechaCreacion,
                null);
    }

    /**
     * La incidencia abierta por un administrador (HU50) con su respuesta, que es obligatoria (hasta 1000 caracteres)
     * porque es lo único que el autor llega a ver: al cerrarla se elimina. Solo se abre una incidencia pendiente.
     */
    public Incidencia abrir(String respuesta) {
        exigirSinResolver();
        if (estado != EstadoIncidencia.PENDIENTE) {
            throw new IncidenciaYaAbiertaException();
        }
        String texto = exigirTexto(respuesta, "una respuesta", RESPUESTA_MAXIMA);
        return new Incidencia(id, autorId, asunto, descripcion, EstadoIncidencia.ABIERTA, texto, fechaCreacion, null);
    }

    /**
     * La incidencia resuelta por un administrador (HU192), con la fecha en la que se resuelve. Solo se resuelve una
     * incidencia abierta y una sola vez.
     */
    public Incidencia resolver(Instant fecha) {
        exigirSinResolver();
        if (estado != EstadoIncidencia.ABIERTA) {
            throw new IncidenciaNoAbiertaException();
        }
        return new Incidencia(id, autorId, asunto, descripcion, EstadoIncidencia.RESUELTA, respuesta, fechaCreacion,
                fecha);
    }

    private void exigirSinResolver() {
        if (estado == EstadoIncidencia.RESUELTA) {
            throw new IncidenciaYaResueltaException();
        }
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