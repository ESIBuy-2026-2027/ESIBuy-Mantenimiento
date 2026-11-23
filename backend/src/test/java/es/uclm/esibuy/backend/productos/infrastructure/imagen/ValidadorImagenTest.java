package es.uclm.esibuy.backend.productos.infrastructure.imagen;

import es.uclm.esibuy.backend.productos.domain.exception.ImagenNoSeguraException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ValidadorImagenTest {

    private final ValidadorImagen validador = new ValidadorImagen();

    @Test
    @DisplayName("Acepta una imagen PNG válida")
    void aceptaPngValido() throws IOException {
        assertDoesNotThrow(() -> validador.validar(imagen("png", 20, 30), "producto.png", "image/png"));
    }

    @Test
    @DisplayName("Acepta una imagen JPEG válida")
    void aceptaJpegValido() throws IOException {
        assertDoesNotThrow(() -> validador.validar(imagen("jpeg", 20, 30), "producto.jpg", "image/jpeg"));
    }

    @Test
    @DisplayName("Rechaza contenido vacío o nulo")
    void rechazaContenidoVacioONulo() {
        assertThrows(ImagenNoSeguraException.class, () -> validador.validar(new byte[0], "a.png", "image/png"));
        assertThrows(ImagenNoSeguraException.class, () -> validador.validar(null, "a.png", "image/png"));
    }

    @Test
    @DisplayName("Rechaza archivos que superan 5 MiB")
    void rechazaArchivoDemasiadoGrande() {
        byte[] contenido = new byte[ValidadorImagen.TAMANO_MAXIMO_BYTES + 1];
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(contenido, "a.png", "image/png"));
    }

    @Test
    @DisplayName("Rechaza extensión no admitida")
    void rechazaExtensionNoAdmitida() throws IOException {
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(imagen("png", 10, 10), "producto.gif", "image/png"));
    }

    @Test
    @DisplayName("Rechaza MIME no admitido o que no coincide con el formato")
    void rechazaMimeIncorrecto() throws IOException {
        byte[] png = imagen("png", 10, 10);
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(png, "producto.png", "image/jpeg"));
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(png, "producto.png", "application/pdf"));
    }

    @Test
    @DisplayName("Rechaza datos que no contienen una imagen decodificable")
    void rechazaContenidoNoImagenOCorrupto() {
        byte[] pdf = "%PDF-1.7 contenido".getBytes(java.nio.charset.StandardCharsets.UTF_8);
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(pdf, "documento.jpg", "image/jpeg"));
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47}, "roto.png", "image/png"));
    }

    @Test
    @DisplayName("Rechaza MIME o extensión falsificados")
    void rechazaMetadatosFalsificados() throws IOException {
        byte[] jpeg = imagen("jpeg", 10, 10);
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(jpeg, "producto.png", "image/png"));
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(jpeg, "producto.jpg", "image/png"));
    }

    @Test
    @DisplayName("Rechaza imágenes que superan los límites de dimensiones")
    void rechazaDimensionesExcesivas() throws IOException {
        byte[] demasiadoAncha = imagen("png", ValidadorImagen.DIMENSION_MAXIMA_PIXELES + 1, 1);
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(demasiadoAncha, "ancha.png", "image/png"));

        // 5001 x 5000 supera 25 millones de píxeles aunque cada lado esté bajo 8000.
        byte[] demasiadosPixeles = imagen("png", 5001, 5000);
        assertThrows(ImagenNoSeguraException.class,
                () -> validador.validar(demasiadosPixeles, "grande.png", "image/png"));
    }

    private byte[] imagen(String formato, int ancho, int alto) throws IOException {
        BufferedImage imagen = new BufferedImage(ancho, alto, BufferedImage.TYPE_BYTE_BINARY);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        if (!ImageIO.write(imagen, formato, salida)) {
            throw new IOException("No hay escritor disponible para " + formato);
        }
        return salida.toByteArray();
    }
}
