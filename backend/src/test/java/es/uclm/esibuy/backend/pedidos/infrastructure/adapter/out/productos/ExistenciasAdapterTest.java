package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.productos;

import es.uclm.esibuy.backend.productos.application.port.in.ExistenciasProductosUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia el stock de los productos (HU10), sin Spring
@ExtendWith(MockitoExtension.class)
class ExistenciasAdapterTest {

    @Mock
    private ExistenciasProductosUseCase existencias;

    @Test
    @DisplayName("HU10-CP15: descontar y reponer el stock se piden al módulo de productos")
    void descontarYReponerSePidenAlModuloDeProductos() {
        when(existencias.descontar("p-1", 2)).thenReturn(true);
        ExistenciasAdapter adapter = new ExistenciasAdapter(existencias);

        assertTrue(adapter.descontar("p-1", 2));
        adapter.reponer("p-1", 2);

        verify(existencias).reponer("p-1", 2);
    }
}
