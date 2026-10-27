package es.uclm.esibuy.backend.categorias.infrastructure.adapter.out.productos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.in.ConteoProductosUseCase;

// Adaptador de categorías hacia el módulo de productos (HU39, T332). El puerto de entrada de productos es
// un doble de prueba (Mockito): se prueba sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU39-CP02, propuesto.
@ExtendWith(MockitoExtension.class)
class ProductosPorCategoriaAdapterTest {

    @Mock
    private ConteoProductosUseCase conteoProductos;

    @Test
    @DisplayName("HU39-CP02: el número de productos se pide al módulo de productos por su puerto de entrada")
    void contarPorCategoriaPreguntaAlModuloDeProductos() {
        Map<String, Long> recuento = Map.of("cat-1", 3L);
        when(conteoProductos.contarPorCategoria()).thenReturn(recuento);

        assertEquals(recuento, new ProductosPorCategoriaAdapter(conteoProductos).contarPorCategoria());
    }
}
