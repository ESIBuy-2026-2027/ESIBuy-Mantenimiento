package es.uclm.esibuy.backend.productos.application.port.out;

// Puerto de salida: analizar la URL de la imagen de un producto con un antivirus externo (HU31, T397)
public interface AnalisisImagenPort {

    ResultadoAnalisis analizar(String urlImagen);
}
