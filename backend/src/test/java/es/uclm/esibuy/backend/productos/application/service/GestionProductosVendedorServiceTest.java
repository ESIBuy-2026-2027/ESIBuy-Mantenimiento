package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.argThat;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

@ExtendWith(MockitoExtension.class)
class GestionProductosVendedorServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU47-CP02: al eliminar la cuenta se conservan como no disponibles los productos con pedidos y se eliminan los demás junto con sus valoraciones")
    void desactivaTodosLosProductosDelVendedor() {
        Producto disponible = producto("p-1", true);
        Producto yaNoDisponible = producto("p-2", false);
        when(productoRepository.listarTodosPorVendedor("v-1")).thenReturn(List.of(disponible, yaNoDisponible));

        List<String> ids = new GestionProductosVendedorService(productoRepository).desactivarTodos("v-1");

        assertEquals(List.of("p-1", "p-2"), ids);
        verify(productoRepository).guardar(argThat(producto -> producto.getId().equals("p-1")
                && !producto.isDisponible()));
    }

    private static Producto producto(String id, boolean disponible) {
        return new Producto(id, "v-1", "Producto " + id, "", BigDecimal.TEN, 1, "cat-1", "",
                disponible, Descuento.sinDescuento(), Descuento.sinDescuento());
    }
}
