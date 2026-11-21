package es.uclm.esibuy.backend.productos.application.port.out;

// Puerto de salida: analizar imágenes de producto mediante un antivirus externo (HU31, T397).
public interface AnalisisImagenPort {

    ResultadoAnalisis analizar(String urlImagen);

    ResultadoAnalisis analizarArchivo(byte[] contenido, String nombreArchivo, String tipoContenido);
}
