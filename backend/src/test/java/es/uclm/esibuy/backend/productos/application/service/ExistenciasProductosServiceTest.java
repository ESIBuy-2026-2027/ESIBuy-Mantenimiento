package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Stock de los productos al realizar un pedido (HU10). El repositorio es un doble de prueba (Mockito): el descuento
// atómico en MongoDB se prueba en ProductoMongoAdapterTest.
@ExtendWith(MockitoExtension.class)
class ExistenciasProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU10-CP15: descontar dice si había stock suficiente para quitar esas unidades")
    void descontarDiceSiHabiaStockSuficiente() {
        when(productoRepository.descontarStock("p-1", 2)).thenReturn(true);
        when(productoRepository.descontarStock("p-2", 9)).thenReturn(false);
        ExistenciasProductosService servicio = new ExistenciasProductosService(productoRepository);

        assertTrue(servicio.descontar("p-1", 2));
        assertFalse(servicio.descontar("p-2", 9));
    }

    @Test
    @DisplayName("HU10-CP15: reponer devuelve las unidades al stock del producto")
    void reponerDevuelveLasUnidades() {
        new ExistenciasProductosService(productoRepository).reponer("p-1", 2);

        verify(productoRepository).reponerStock("p-1", 2);
    }

    @Test
    @DisplayName("HU10-CP15: no se descuentan ni se reponen cantidades menores que 1")
    void noSeUsanCantidadesMenoresQueUno() {
        ExistenciasProductosService servicio = new ExistenciasProductosService(productoRepository);

        assertThrows(ProductoInvalidoException.class, () -> servicio.descontar("p-1", 0));
        assertThrows(ProductoInvalidoException.class, () -> servicio.reponer("p-1", -1));
        verify(productoRepository, never()).descontarStock(anyString(), anyInt());
        verify(productoRepository, never()).reponerStock(anyString(), anyInt());
    }
}
