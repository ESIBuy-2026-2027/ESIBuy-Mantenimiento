package es.uclm.esibuy.backend.valoraciones.application.port.out;

// Puerto de salida: dejar en el producto la media y el número de sus valoraciones (HU13), para el catálogo
public interface ProductosPort {

    void actualizarValoracion(String productoId, double media, int numero);
}
