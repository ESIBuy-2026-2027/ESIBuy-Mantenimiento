package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import java.time.Instant;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Forma en que se guarda una categoría en MongoDB (colección categorias, con su validación en
 * ColeccionesEsiBuy). El estado se guarda como texto (ACTIVA o INACTIVA). Las categorías guardadas antes
 * de la T321 no tienen descripción ni estado: el mapper las lee como ACTIVAS y sin descripción.
 */
@Document(collection = "categorias")
public class CategoriaDocument {

    @Id
    private String id;
    private String nombre;
    private String descripcion;
    private String estado;

    @CreatedDate
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }
    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
