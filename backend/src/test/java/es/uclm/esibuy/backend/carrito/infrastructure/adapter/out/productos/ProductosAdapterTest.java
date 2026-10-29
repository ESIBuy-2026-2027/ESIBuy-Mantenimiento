package es.uclm.esibuy.backend.carrito.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.carrito.application.port.out.ProductoCarrito;
import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Adaptador del carrito hacia el módulo de productos (HU9), sin Spring: traduce su respuesta a la del carrito
@ExtendWith(MockitoExtension.class)
class ProductosAdapterTest {

    @Mock
    private CatalogoProductosUseCase catalogo;

    @Test
    @DisplayName("HU9-CP19: los datos y el precio final del producto se piden al módulo de productos")
    void consultarComprablePreguntaAlModuloDeProductos() {
        when(catalogo.consultarComprable("p-1", true)).thenReturn(Optional.of(
                new ProductoComercial("p-1", "u-2", "Teclado", "t.png", new BigDecimal("40.00"), 7)));

        assertEquals(Optional.of(new ProductoCarrito("p-1", "u-2", "Teclado", "t.png", new BigDecimal("40.00"), 7)),
                new ProductosAdapter(catalogo).consultarComprable("p-1", true));
    }

    @Test
    @DisplayName("HU9-CP12: un producto que productos no deja comprar tampoco se puede comprar desde el carrito")
    void unProductoNoComprableSigueSinSerlo() {
        when(catalogo.consultarComprable("p-1", false)).thenReturn(Optional.empty());

        assertTrue(new ProductosAdapter(catalogo).consultarComprable("p-1", false).isEmpty());
    }
}
