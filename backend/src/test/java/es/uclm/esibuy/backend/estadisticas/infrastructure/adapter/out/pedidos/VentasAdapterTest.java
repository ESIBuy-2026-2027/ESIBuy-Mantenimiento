package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.pedidos;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.estadisticas.domain.model.VentaRegistrada;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentaProducto;
import es.uclm.esibuy.backend.pedidos.application.port.in.VentasVendedorUseCase;

// Adaptador de estadísticas hacia las ventas del módulo de pedidos (HU17, T279), sin Spring
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class VentasAdapterTest {

    @Mock
    private VentasVendedorUseCase ventasVendedor;

    @Test
    @DisplayName("HU17-CP13: las ventas del vendedor se piden al módulo de pedidos y llegan sin perder datos")
    void lasVentasSePidenAlModuloDePedidos() {
        when(ventasVendedor.ventasDe("v-1")).thenReturn(List.of(
                new VentaProducto("o-1", "p-1", "Teclado", 2, new BigDecimal("40.00"))));

        assertEquals(List.of(new VentaRegistrada("o-1", "p-1", "Teclado", 2, new BigDecimal("40.00"))),
                new VentasAdapter(ventasVendedor).ventasDe("v-1"));
    }
}
