package es.uclm.esibuy.backend.productos.application.port.out;

import java.util.Optional;

/** Puerto de salida para almacenar y recuperar el contenido binario de imágenes. */
public interface ImagenStoragePort {

    String almacenar(byte[] contenido, String nombreArchivo, String tipoContenido);

    Optional<ImagenAlmacenada> obtener(String id);

    void eliminar(String id);

    /** Contenido y metadatos necesarios para servir una imagen almacenada. */
    record ImagenAlmacenada(byte[] contenido, String tipoContenido, String nombreArchivo) {
        public ImagenAlmacenada {
            contenido = contenido.clone();
        }

        @Override
        public byte[] contenido() {
            return contenido.clone();
        }
    }
}
