package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda una categoría en MongoDB (colección categorias, con su validación en
 * ColeccionesEsiBuy). La descripción y el estado del modelo de datos todavía no los usa nadie.
 */
@Document(collection = "categorias")
public class CategoriaDocument {

    @Id
    private String id;
    private String nombre;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
