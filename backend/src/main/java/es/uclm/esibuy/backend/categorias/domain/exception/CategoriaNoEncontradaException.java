package es.uclm.esibuy.backend.categorias.domain.exception;

/** No existe una categoría con ese identificador (HU41). */
public class CategoriaNoEncontradaException extends RuntimeException {

    public CategoriaNoEncontradaException() {
        super("La categoría no existe");
    }
}
