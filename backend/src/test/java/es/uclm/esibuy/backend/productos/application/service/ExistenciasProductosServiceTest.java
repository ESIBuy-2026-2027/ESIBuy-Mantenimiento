package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.out.AlertasFavoritosPort;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.model.DatosProducto;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

// Stock de los productos al realizar un pedido (HU10). El repositorio es un doble de prueba (Mockito): el descuento
// atómico en MongoDB se prueba en ProductoMongoAdapterTest. Al reponer stock (cancelación de un pedido) se avisa a
// las alertas de los favoritos (AB#529), también un doble.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP21, propuesto.
@ExtendWith(MockitoExtension.class)
class ExistenciasProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private AlertasFavoritosPort alertas;

    private ExistenciasProductosService servicio;

    @BeforeEach
    void preparar() {
        servicio = new ExistenciasProductosService(productoRepository, alertas);
    }

    @Test
    @DisplayName("HU10-CP15: descontar dice si había stock suficiente para quitar esas unidades")
    void descontarDiceSiHabiaStockSuficiente() {
        when(productoRepository.descontarStock("p-1", 2)).thenReturn(true);
        when(productoRepository.descontarStock("p-2", 9)).thenReturn(false);

        assertTrue(servicio.descontar("p-1", 2));
        assertFalse(servicio.descontar("p-2", 9));
        verifyNoInteractions(alertas);
    }

    @Test
    @DisplayName("HU10-CP15: reponer devuelve las unidades al stock del producto")
    void reponerDevuelveLasUnidades() {
        servicio.reponer("p-1", 2);

        verify(productoRepository).reponerStock("p-1", 2);
    }

    @Test
    @DisplayName("HU10-CP15: no se descuentan ni se reponen cantidades menores que 1")
    void noSeUsanCantidadesMenoresQueUno() {
        assertThrows(ProductoInvalidoException.class, () -> servicio.descontar("p-1", 0));
        assertThrows(ProductoInvalidoException.class, () -> servicio.reponer("p-1", -1));
        verify(productoRepository, never()).descontarStock(anyString(), anyInt());
        verify(productoRepository, never()).reponerStock(anyString(), anyInt());
    }

    @Test
    @DisplayName("HU529-CP21: al reponer stock se avisa a las alertas con el producto como estaba y como queda")
    void reponerAvisaALasAlertas() {
        Producto agotado = producto(0);
        when(productoRepository.reponerStock("p-1", 2)).thenReturn(Optional.of(agotado));

        servicio.reponer("p-1", 2);

        ArgumentCaptor<Producto> despues = ArgumentCaptor.forClass(Producto.class);
        verify(alertas).notificarCambio(same(agotado), despues.capture());
        assertEquals(2, despues.getValue().getStock());
        assertEquals(agotado.getId(), despues.getValue().getId());
    }

    @Test
    @DisplayName("HU529-CP21: si el producto ya no existe, reponer no avisa a las alertas")
    void reponerUnProductoQueNoExisteNoAvisa() {
        when(productoRepository.reponerStock("p-404", 2)).thenReturn(Optional.empty());

        servicio.reponer("p-404", 2);

        verify(alertas, never()).notificarCambio(any(), any());
    }

    private static Producto producto(int stock) {
        return new Producto("p-1", new DatosProducto(
                new DatosProducto.DatosBasicos("v-1", "Teclado", "", new BigDecimal("50.00"), stock, "cat-1", ""),
                new DatosProducto.Configuracion(true, Descuento.sinDescuento(), Descuento.sinDescuento()),
                new DatosProducto.Valoraciones(null, 0)));
    }
}
