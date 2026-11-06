package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.incidencias.domain.model.EstadoIncidencia;
import es.uclm.esibuy.backend.incidencias.domain.model.Incidencia;

/**
 * Convierte entre la incidencia del dominio y su documento de MongoDB: el autorId del dominio es el usuarioId del
 * documento y el estado se guarda como texto. Al leer se vuelve a pasar por el constructor de Incidencia, así que un
 * documento que no cumpla las reglas del dominio se rechaza.
 */
public class IncidenciaDocumentMapper {

    public IncidenciaDocument aDocumento(Incidencia incidencia) {
        IncidenciaDocument documento = new IncidenciaDocument();
        documento.setId(incidencia.id());
        documento.setUsuarioId(incidencia.autorId());
        documento.setAsunto(incidencia.asunto());
        documento.setDescripcion(incidencia.descripcion());
        documento.setEstado(incidencia.estado().name());
        documento.setRespuesta(incidencia.respuesta());
        documento.setFechaCreacion(incidencia.fechaCreacion());
        return documento;
    }

    public Incidencia aDominio(IncidenciaDocument documento) {
        return new Incidencia(documento.getId(), documento.getUsuarioId(), documento.getAsunto(),
                documento.getDescripcion(), EstadoIncidencia.valueOf(documento.getEstado()),
                documento.getRespuesta(), documento.getFechaCreacion());
    }
}