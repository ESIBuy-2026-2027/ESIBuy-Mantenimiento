
package es.uclm.esibuy.backend.productos.infrastructure.imagen;

import es.uclm.esibuy.backend.productos.domain.exception.ImagenNoSeguraException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

/**
 * Valida las imágenes recibidas antes de almacenarlas.
 *
 * La comprobación combina extensión, MIME declarado, firma/formato detectado
 * y decodificación real. Ninguno de los metadatos proporcionados por el cliente
 * se considera suficiente por sí solo.
 */
public class ValidadorImagen {

    public static final int TAMANO_MAXIMO_BYTES = 5 * 1024 * 1024;
    public static final int DIMENSION_MAXIMA_PIXELES = 8_000;
    public static final long PIXELES_MAXIMOS = 25_000_000L;

    private static final Set<String> EXTENSIONES_ADMITIDAS = Set.of(".jpg", ".jpeg", ".png");

    public void validar(byte[] contenido, String nombreArchivo, String tipoContenido) {
        validarContenido(contenido);

        String extension = obtenerExtension(nombreArchivo);
        validarExtension(extension);

        String mime = normalizarMime(tipoContenido);
        validarMime(mime);

        try {
            BufferedImage imagen = ImageIO.read(new ByteArrayInputStream(contenido));
            if (imagen == null) {
                throw new ImagenNoSeguraException();
            }

            String formato = detectarFormato(contenido);
            validarFormato(formato, extension, mime);
            validarDimensiones(imagen);

        } catch (IOException | RuntimeException excepcion) {
            if (excepcion instanceof ImagenNoSeguraException imagenNoSegura) {
                throw imagenNoSegura;
            }
            throw new ImagenNoSeguraException();
        }
    }

    private void validarContenido(byte[] contenido) {
        if (contenido == null || contenido.length == 0
                || contenido.length > TAMANO_MAXIMO_BYTES) {
            throw new ImagenNoSeguraException();
        }
    }

    private void validarExtension(String extension) {
        if (!EXTENSIONES_ADMITIDAS.contains(extension)) {
            throw new ImagenNoSeguraException();
        }
    }

    private String normalizarMime(String tipoContenido) {
        return tipoContenido == null
                ? ""
                : tipoContenido.trim().toLowerCase(Locale.ROOT);
    }

    private void validarMime(String mime) {
        if (!mime.equals("image/jpeg") && !mime.equals("image/png")) {
            throw new ImagenNoSeguraException();
        }
    }

    private void validarFormato(String formato, String extension, String mime) {
        boolean esJpeg = "jpeg".equals(formato);
        boolean esPng = "png".equals(formato);

        if (!esJpeg && !esPng) {
            throw new ImagenNoSeguraException();
        }

        if (esJpeg && (!mime.equals("image/jpeg")
                || !(extension.equals(".jpg") || extension.equals(".jpeg")))) {
            throw new ImagenNoSeguraException();
        }

        if (esPng && (!mime.equals("image/png") || !extension.equals(".png"))) {
            throw new ImagenNoSeguraException();
        }
    }

    private void validarDimensiones(BufferedImage imagen) {
        int ancho = imagen.getWidth();
        int alto = imagen.getHeight();

        if (ancho <= 0 || alto <= 0
                || ancho > DIMENSION_MAXIMA_PIXELES
                || alto > DIMENSION_MAXIMA_PIXELES
                || (long) ancho * alto > PIXELES_MAXIMOS) {
            throw new ImagenNoSeguraException();
        }
    }

    private String obtenerExtension(String nombreArchivo) {
        if (nombreArchivo == null) {
            return "";
        }

        int separador = Math.max(
                nombreArchivo.lastIndexOf('/'),
                nombreArchivo.lastIndexOf('\\'));

        String nombre = nombreArchivo.substring(separador + 1)
                .toLowerCase(Locale.ROOT);

        int punto = nombre.lastIndexOf('.');
        return punto < 0 ? "" : nombre.substring(punto);
    }

    private String detectarFormato(byte[] contenido) throws IOException {
        // ImageIO reconoce el formato a partir del contenido, no del nombre/MIME.
        try (var entrada = ImageIO.createImageInputStream(
                new ByteArrayInputStream(contenido))) {

            if (entrada == null) {
                return "";
            }

            var lectores = ImageIO.getImageReaders(entrada);
            if (!lectores.hasNext()) {
                return "";
            }

            var lector = lectores.next();
            try {
                return lector.getFormatName().toLowerCase(Locale.ROOT);
            } finally {
                lector.dispose();
            }
        }
    }
}
