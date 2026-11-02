package es.uclm.esibuy.backend.categorias.domain.exception;

/** La categoría es la principal de algún vendedor y no se puede eliminar (HU42). */
public class CategoriaConVendedoresException extends RuntimeException {

    public CategoriaConVendedoresException() {
        super("La categoría es la principal de algún vendedor y no se puede eliminar");
    }
}