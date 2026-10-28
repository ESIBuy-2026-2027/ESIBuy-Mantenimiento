package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.categorias.domain.model.Categoria;
import es.uclm.esibuy.backend.categorias.domain.model.EstadoCategoria;

/**
 * Convierte entre la entidad de dominio y el documento de MongoDB. Al leer se vuelve a pasar por el
 * constructor de Categoria, así que un documento que no cumpla las reglas del dominio se rechaza.
 */
public class CategoriaDocumentMapper {

    public CategoriaDocument aDocumento(Categoria categoria) {
        CategoriaDocument documento = new CategoriaDocument();
        documento.setId(categoria.id());
        documento.setNombre(categoria.nombre());
        documento.setDescripcion(categoria.descripcion());
        documento.setEstado(categoria.estado().name());
        return documento;
    }

    // Las categorías guardadas antes de la T321 no tienen estado: se leen como ACTIVAS. Sin descripción,
    // el dominio la deja vacía.
    public Categoria aDominio(CategoriaDocument documento) {
        EstadoCategoria estado = documento.getEstado() == null
                ? EstadoCategoria.ACTIVA
                : EstadoCategoria.valueOf(documento.getEstado());
        return new Categoria(documento.getId(), documento.getNombre(), documento.getDescripcion(), estado);
    }
}
