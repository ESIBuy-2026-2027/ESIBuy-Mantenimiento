package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda una incidencia en MongoDB (colección incidencias, HU44). El autor se guarda como
 * usuarioId, el nombre del esquema, y el estado como texto.
 */
@Document(collection = "incidencias")
public class IncidenciaDocument {

    @Id
    private String id;
    private String usuarioId;
    private String asunto;
    private String descripcion;
    private String estado;
    private String respuesta;
    private Instant fechaCreacion;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public String getAsunto() { return asunto; }
    public void setAsunto(String asunto) { this.asunto = asunto; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public String getRespuesta() { return respuesta; }
    public void setRespuesta(String respuesta) { this.respuesta = respuesta; }
    public Instant getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(Instant fechaCreacion) { this.fechaCreacion = fechaCreacion; }
}