package es.uclm.esibuy.backend.categorias.domain.exception;

/** La categoría tiene productos asociados y no se puede eliminar (HU42). */
public class CategoriaConProductosException extends RuntimeException {

    public CategoriaConProductosException() {
        super("La categoría tiene productos y no se puede eliminar");
    }
}
