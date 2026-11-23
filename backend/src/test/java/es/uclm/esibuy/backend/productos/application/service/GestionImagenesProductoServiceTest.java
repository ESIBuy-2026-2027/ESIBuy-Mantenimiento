package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.Optional;

import javax.imageio.ImageIO;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.out.*;
import es.uclm.esibuy.backend.productos.domain.exception.*;
import es.uclm.esibuy.backend.productos.domain.model.*;
import es.uclm.esibuy.backend.productos.infrastructure.imagen.ValidadorImagen;

@ExtendWith(MockitoExtension.class)
class GestionImagenesProductoServiceTest {

    @Mock private ProductoRepositoryPort productos;
    @Mock private ImagenStoragePort almacenamiento;
    @Mock private AnalisisImagenPort antivirus;
    @Mock private UsuarioEnSesionPort usuarioEnSesion;

    private GestionImagenesProductoService servicio;
    private byte[] png;

    @BeforeEach
    void preparar() throws Exception {
        servicio = new GestionImagenesProductoService(productos, almacenamiento, antivirus,
                new ValidadorImagen(), usuarioEnSesion);
        BufferedImage imagen = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream salida = new ByteArrayOutputStream();
        ImageIO.write(imagen, "png", salida);
        png = salida.toByteArray();
        when(usuarioEnSesion.idPorCorreo("vendedor@esibuy.test")).thenReturn("v-1");
    }

    @Test
    void almacenaLaImagenSeguraYActualizaElProducto() {
        Producto producto = producto("p-1", "v-1", "");
        when(productos.buscarPorId("p-1")).thenReturn(Optional.of(producto));
        when(antivirus.analizarArchivo(png, "foto.png", "image/png")).thenReturn(ResultadoAnalisis.SEGURA);
        when(almacenamiento.almacenar(png, "foto.png", "image/png")).thenReturn("0123456789abcdef01234567");
        when(productos.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        String referencia = servicio.subirImagen("vendedor@esibuy.test", "p-1", png, "foto.png", "image/png");

        assertEquals("/api/product-images/0123456789abcdef01234567", referencia);
        verify(productos).guardar(argThat(p -> p.getImagen().equals(referencia)));
    }

    @Test
    void noAnalizaNiAlmacenaUnaImagenConFormatoInvalido() {
        when(productos.buscarPorId("p-1")).thenReturn(Optional.of(producto("p-1", "v-1", "")));

        assertThrows(ImagenNoSeguraException.class,
                () -> servicio.subirImagen("vendedor@esibuy.test", "p-1", png, "foto.jpg", "image/jpeg"));

        verifyNoInteractions(antivirus, almacenamiento);
        verify(productos, never()).guardar(any());
    }

    @Test
    void rechazaUnaImagenMarcadaComoMaliciosa() {
        when(productos.buscarPorId("p-1")).thenReturn(Optional.of(producto("p-1", "v-1", "")));
        when(antivirus.analizarArchivo(png, "foto.png", "image/png")).thenReturn(ResultadoAnalisis.MALICIOSA);

        assertThrows(ImagenNoSeguraException.class,
                () -> servicio.subirImagen("vendedor@esibuy.test", "p-1", png, "foto.png", "image/png"));

        verifyNoInteractions(almacenamiento);
        verify(productos, never()).guardar(any());
    }

    @Test
    void noPermiteSubirImagenAUnProductoDeOtroVendedor() {
        when(productos.buscarPorId("p-1")).thenReturn(Optional.of(producto("p-1", "v-2", "")));

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.subirImagen("vendedor@esibuy.test", "p-1", png, "foto.png", "image/png"));

        verifyNoInteractions(antivirus, almacenamiento);
    }

    @Test
    void eliminaLaImagenAnteriorSoloDespuesDeGuardarLaNueva() {
        Producto producto = producto("p-1", "v-1", "/api/product-images/aaaaaaaaaaaaaaaaaaaaaaaa");
        when(productos.buscarPorId("p-1")).thenReturn(Optional.of(producto));
        when(antivirus.analizarArchivo(png, "foto.png", "image/png")).thenReturn(ResultadoAnalisis.SEGURA);
        when(almacenamiento.almacenar(png, "foto.png", "image/png")).thenReturn("0123456789abcdef01234567");
        when(productos.guardar(any())).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.subirImagen("vendedor@esibuy.test", "p-1", png, "foto.png", "image/png");

        verify(almacenamiento).eliminar("aaaaaaaaaaaaaaaaaaaaaaaa");
    }

    private static Producto producto(String id, String vendedorId, String imagen) {
        return new Producto(id, new DatosProducto(
                new DatosProducto.DatosBasicos(vendedorId, "Producto", "", java.math.BigDecimal.TEN, 1, "cat-1", imagen),
                new DatosProducto.Configuracion(true, Descuento.sinDescuento(), Descuento.sinDescuento()),
                new DatosProducto.Valoraciones(null, 0)));
    }
}
