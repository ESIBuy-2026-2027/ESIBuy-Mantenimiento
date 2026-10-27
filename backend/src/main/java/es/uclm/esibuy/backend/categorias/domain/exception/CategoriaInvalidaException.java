package es.uclm.esibuy.backend.categorias.domain.exception;

public class CategoriaInvalidaException extends RuntimeException {

    public CategoriaInvalidaException(String mensaje) {
        super(mensaje);
    }

}
