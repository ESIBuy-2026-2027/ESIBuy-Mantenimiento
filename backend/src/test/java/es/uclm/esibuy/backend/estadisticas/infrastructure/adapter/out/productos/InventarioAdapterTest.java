package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.estadisticas.domain.model.ProductoInventariado;
import es.uclm.esibuy.backend.productos.application.port.in.InventarioVendedorUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoInventario;

// Adaptador de estadísticas hacia el inventario del módulo de productos (HU17, T279), sin Spring
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class InventarioAdapterTest {

    @Mock
    private InventarioVendedorUseCase inventarioVendedor;

    @Test
    @DisplayName("HU17-CP13: los productos del vendedor se piden al módulo de productos y llegan sin perder datos")
    void losProductosSePidenAlModuloDeProductos() {
        when(inventarioVendedor.productosDe("v-1")).thenReturn(List.of(
                new ProductoInventario("p-1", "Teclado", 3, true, 4.5, 2),
                new ProductoInventario("p-2", "Ratón", 0, false, null, 0)));

        assertEquals(List.of(
                new ProductoInventariado("p-1", "Teclado", 3, true, 4.5, 2),
                new ProductoInventariado("p-2", "Ratón", 0, false, null, 0)),
                new InventarioAdapter(inventarioVendedor).productosDe("v-1"));
    }
}
