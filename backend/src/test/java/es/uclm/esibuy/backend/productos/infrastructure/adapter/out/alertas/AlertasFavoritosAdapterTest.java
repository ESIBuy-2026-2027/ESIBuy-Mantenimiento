package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.alertas;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto;
import es.uclm.esibuy.backend.alertas.application.port.in.GeneracionAlertasUseCase;
import es.uclm.esibuy.backend.productos.domain.model.DatosProducto;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

// Adaptador de los productos hacia el módulo de alertas (AB#529), sin Spring ni MongoDB: traduce el producto antes y
// después del cambio, y un fallo al generar las alertas no deshace el cambio del producto, que ya está guardado.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP24, propuesto.
@ExtendWith(MockitoExtension.class)
class AlertasFavoritosAdapterTest {

    @Mock
    private GeneracionAlertasUseCase generacionAlertas;

    @Test
    @DisplayName("HU529-CP24: envía a las alertas el nombre, la imagen, el precio estándar y el premium y si se puede comprar, antes y después")
    void traduceElCambioDelProducto() {
        Producto antes = producto(new BigDecimal("100.00"), 0, Descuento.sinDescuento());
        Producto despues = producto(new BigDecimal("100.00"), 5, new Descuento(true, new BigDecimal("20")));

        new AlertasFavoritosAdapter(generacionAlertas).notificarCambio(antes, despues);

        ArgumentCaptor<CambioProducto> captor = ArgumentCaptor.forClass(CambioProducto.class);
        verify(generacionAlertas).notificarCambio(captor.capture());
        CambioProducto cambio = captor.getValue();
        assertAll(
                () -> assertEquals("p-1", cambio.productoId()),
                () -> assertEquals("Teclado", cambio.nombreProducto()),
                () -> assertEquals("https://img.test/teclado.png", cambio.imagenProducto()),
                () -> assertEquals(new BigDecimal("100.00"), cambio.antes().precioEstandar()),
                () -> assertEquals(new BigDecimal("100.00"), cambio.antes().precioPremium()),
                () -> assertFalse(cambio.antes().comprable()),
                () -> assertEquals(new BigDecimal("100.00"), cambio.despues().precioEstandar()),
                () -> assertEquals(new BigDecimal("80.00"), cambio.despues().precioPremium()),
                () -> assertTrue(cambio.despues().comprable()));
    }

    @Test
    @DisplayName("HU529-CP24: si generar las alertas falla, el error no llega al vendedor ni al cliente")
    void unFalloDeLasAlertasNoSePropaga() {
        doThrow(new IllegalStateException("MongoDB no responde")).when(generacionAlertas).notificarCambio(any());
        Producto producto = producto(new BigDecimal("100.00"), 1, Descuento.sinDescuento());

        assertDoesNotThrow(() -> new AlertasFavoritosAdapter(generacionAlertas).notificarCambio(producto, producto));
    }

    private static Producto producto(BigDecimal precio, int stock, Descuento descuentoPremium) {
        return new Producto("p-1", new DatosProducto(
                new DatosProducto.DatosBasicos("v-1", "Teclado", "", precio, stock, "cat-1",
                        "https://img.test/teclado.png"),
                new DatosProducto.Configuracion(true, Descuento.sinDescuento(), descuentoPremium),
                new DatosProducto.Valoraciones(null, 0)));
    }
}
