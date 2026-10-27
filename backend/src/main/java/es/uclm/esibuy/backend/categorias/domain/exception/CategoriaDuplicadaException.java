package es.uclm.esibuy.backend.categorias.domain.exception;

/** Ya hay una categoría con ese nombre (sin distinguir mayúsculas ni tildes, HU40). */
public class CategoriaDuplicadaException extends RuntimeException {

    private static final String MENSAJE = "Ya existe una categoría con ese nombre";

    public CategoriaDuplicadaException() {
        super(MENSAJE);
    }

    public CategoriaDuplicadaException(Throwable causa) {
        super(MENSAJE, causa);
    }
}
