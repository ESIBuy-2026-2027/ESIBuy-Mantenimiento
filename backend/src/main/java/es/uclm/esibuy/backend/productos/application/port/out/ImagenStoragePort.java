
package es.uclm.esibuy.backend.productos.application.port.out;

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * Puerto de salida para almacenar y recuperar el contenido binario de imágenes.
 */
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

        @Override
        public boolean equals(Object objeto) {
            if (this == objeto) {
                return true;
            }

            if (!(objeto instanceof ImagenAlmacenada(byte[] otroContenido, String otroTipoContenido, String otroNombreArchivo))) {
                return false;
            }

            return Arrays.equals(contenido, otroContenido)
                    && Objects.equals(tipoContenido, otroTipoContenido)
                    && Objects.equals(nombreArchivo, otroNombreArchivo);
        }

        @Override
        public int hashCode() {
            int resultado = Arrays.hashCode(contenido);
            resultado = 31 * resultado + Objects.hashCode(tipoContenido);
            resultado = 31 * resultado + Objects.hashCode(nombreArchivo);
            return resultado;
        }

        @Override
        public String toString() {
            return "ImagenAlmacenada[contenido=" + Arrays.toString(contenido)
                    + ", tipoContenido=" + tipoContenido
                    + ", nombreArchivo=" + nombreArchivo + "]";
        }
    }
}
