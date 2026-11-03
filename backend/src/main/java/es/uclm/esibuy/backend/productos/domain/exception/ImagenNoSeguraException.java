package es.uclm.esibuy.backend.productos.domain.exception;

// La imagen de un producto no ha superado el análisis antivirus (HU31, T397)
public class ImagenNoSeguraException extends RuntimeException {

    public ImagenNoSeguraException() {
        super("La imagen no ha superado el análisis antivirus");
    }
}
