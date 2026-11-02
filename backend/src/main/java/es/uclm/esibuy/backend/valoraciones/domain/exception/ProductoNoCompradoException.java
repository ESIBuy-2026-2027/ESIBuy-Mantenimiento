package es.uclm.esibuy.backend.valoraciones.domain.exception;

/** El cliente intenta valorar un producto que no ha comprado (HU13): solo se valora lo comprado. */
public class ProductoNoCompradoException extends RuntimeException {

    public ProductoNoCompradoException() {
        super("Solo puedes valorar productos que has comprado");
    }
}
