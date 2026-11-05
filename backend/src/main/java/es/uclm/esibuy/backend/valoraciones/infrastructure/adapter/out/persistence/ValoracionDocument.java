package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.persistence;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda una valoración en MongoDB (colección valoraciones, HU13). El cliente se guarda como
 * usuarioId, el nombre del esquema. El índice único (usuarioId, productoId) lo crea InicializadorColecciones.
 */
@Document(collection = "valoraciones")
public class ValoracionDocument {

    @Id
    private String id;
    private String productoId;
    private String usuarioId;
    private int puntuacion;
    private String comentario;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getProductoId() { return productoId; }
    public void setProductoId(String productoId) { this.productoId = productoId; }
    public String getUsuarioId() { return usuarioId; }
    public void setUsuarioId(String usuarioId) { this.usuarioId = usuarioId; }
    public int getPuntuacion() { return puntuacion; }
    public void setPuntuacion(int puntuacion) { this.puntuacion = puntuacion; }
    public String getComentario() { return comentario; }
    public void setComentario(String comentario) { this.comentario = comentario; }
}
