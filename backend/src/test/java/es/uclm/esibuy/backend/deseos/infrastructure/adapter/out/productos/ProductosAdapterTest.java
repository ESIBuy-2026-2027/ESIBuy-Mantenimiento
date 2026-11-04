package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.productos.application.port.in.CatalogoProductosUseCase;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Adaptador de la lista de deseos hacia el módulo de productos (HU14), sin Spring
@ExtendWith(MockitoExtension.class)
class ProductosAdapterTest {

    @Mock
    private CatalogoProductosUseCase catalogo;

    @Test
    @DisplayName("HU14-CP04: un producto que el módulo de productos devuelve está disponible")
    void unProductoQueDevuelveProductosEstaDisponible() {
        when(catalogo.consultarComprable("p-1", false)).thenReturn(Optional.of(
                new ProductoComercial("p-1", "u-2", "Teclado", "t.png", new BigDecimal("40.00"), 7)));

        assertTrue(new ProductosAdapter(catalogo).estaDisponible("p-1"));
    }

    @Test
    @DisplayName("HU14-CP04: un producto que el módulo de productos no devuelve no está disponible")
    void unProductoQueNoDevuelveProductosNoEstaDisponible() {
        when(catalogo.consultarComprable("p-9", false)).thenReturn(Optional.empty());

        assertFalse(new ProductosAdapter(catalogo).estaDisponible("p-9"));
    }
}
