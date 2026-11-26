package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/**
 * Convierte entre la incidencia del dominio y su documento de MongoDB: el autorId del dominio es el usuarioId del
 * documento y el estado se guarda como texto. Una incidencia resuelta (HU192) se guarda como ABIERTA con su
 * fechaResolucion: la validación de la colección no admite otro estado, y así una versión anterior de la aplicación
 * que comparta la base de datos la sigue pudiendo leer. Al leer se vuelve a pasar por el constructor de Incidencia,
 * así que un documento que no cumpla las reglas del dominio se rechaza.
 */
public class IncidenciaDocumentMapper {

    public IncidenciaDocument aDocumento(Incidencia incidencia) {
        IncidenciaDocument documento = new IncidenciaDocument();
        documento.setId(incidencia.id());
        documento.setUsuarioId(incidencia.autorId());
        documento.setAsunto(incidencia.asunto());
        documento.setDescripcion(incidencia.descripcion());
        documento.setEstado(estadoGuardado(incidencia.estado()).name());
        documento.setRespuesta(incidencia.respuesta());
        documento.setFechaCreacion(incidencia.fechaCreacion());
        documento.setFechaResolucion(incidencia.fechaResolucion());
        return documento;
    }

    public Incidencia aDominio(IncidenciaDocument documento) {
        return new Incidencia(documento.getId(), documento.getUsuarioId(), documento.getAsunto(),
                documento.getDescripcion(), estadoLeido(documento), documento.getRespuesta(),
                documento.getFechaCreacion(), documento.getFechaResolucion());
    }

    private static EstadoIncidencia estadoGuardado(EstadoIncidencia estado) {
        return estado == EstadoIncidencia.RESUELTA ? EstadoIncidencia.ABIERTA : estado;
    }

    private static EstadoIncidencia estadoLeido(IncidenciaDocument documento) {
        EstadoIncidencia guardado = EstadoIncidencia.valueOf(documento.getEstado());
        boolean resuelta = guardado == EstadoIncidencia.ABIERTA && documento.getFechaResolucion() != null;
        return resuelta ? EstadoIncidencia.RESUELTA : guardado;
    }
}