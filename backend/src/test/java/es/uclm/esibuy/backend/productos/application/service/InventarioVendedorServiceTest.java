package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.in.ProductoInventario;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

// Productos de un vendedor para sus estadísticas (HU17, T280). El repositorio es un doble de prueba (Mockito): la
// consulta en MongoDB se prueba en ProductoMongoAdapterTest.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP08, propuesto.
@ExtendWith(MockitoExtension.class)
class InventarioVendedorServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU17-CP08: el inventario del vendedor da el stock, la disponibilidad y las valoraciones de cada producto")
    void elInventarioDaStockDisponibilidadYValoraciones() {
        when(productoRepository.buscarTodosPorVendedor("v-1")).thenReturn(List.of(
                producto("p-1", "Teclado", 3, true, 4.5, 2),
                producto("p-2", "Ratón", 0, false, null, 0)));

        List<ProductoInventario> inventario = new InventarioVendedorService(productoRepository).productosDe("v-1");

        assertEquals(List.of(
                new ProductoInventario("p-1", "Teclado", 3, true, 4.5, 2),
                new ProductoInventario("p-2", "Ratón", 0, false, null, 0)), inventario);
    }

    @Test
    @DisplayName("HU17-CP08: un vendedor sin productos tiene el inventario vacío")
    void sinProductosElInventarioEstaVacio() {
        when(productoRepository.buscarTodosPorVendedor("v-1")).thenReturn(List.of());

        assertEquals(List.of(), new InventarioVendedorService(productoRepository).productosDe("v-1"));
    }

    private static Producto producto(String id, String nombre, int stock, boolean disponible, Double media,
            int numeroValoraciones) {
        return new Producto(id, "v-1", nombre, "", new BigDecimal("10"), stock, "cat-1", "", disponible,
                Descuento.sinDescuento(), Descuento.sinDescuento(), media, numeroValoraciones);
    }
}
