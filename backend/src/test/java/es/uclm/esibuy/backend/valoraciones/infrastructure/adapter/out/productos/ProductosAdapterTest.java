package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.productos.application.port.in.ValoracionesProductoUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

// Adaptador de valoraciones hacia el módulo de productos (HU13, T415), sin Spring. Usa su puerto de entrada, sin
// usar su dominio.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP28, propuesto.
@ExtendWith(MockitoExtension.class)
class ProductosAdapterTest {

    @Mock
    private ValoracionesProductoUseCase valoracionesProducto;

    @Test
    @DisplayName("HU13-CP28: la media y el número de valoraciones se le pasan al módulo de productos")
    void laMediaYElNumeroSeLePasanAlModuloDeProductos() {
        new ProductosAdapter(valoracionesProducto).actualizarValoracion("p-1", 4.5, 2);

        verify(valoracionesProducto).actualizarValoracion("p-1", 4.5, 2);
    }
}
