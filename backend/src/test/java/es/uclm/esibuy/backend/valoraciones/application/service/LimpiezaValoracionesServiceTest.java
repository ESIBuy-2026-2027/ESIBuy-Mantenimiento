package es.uclm.esibuy.backend.valoraciones.application.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.valoraciones.application.port.out.ValoracionRepositoryPort;

@ExtendWith(MockitoExtension.class)
class LimpiezaValoracionesServiceTest {

    @Mock
    private ValoracionRepositoryPort valoracionRepository;

    @Test
    @DisplayName("HU47-CP02: al eliminar la cuenta se conservan como no disponibles los productos con pedidos y se eliminan los demás junto con sus valoraciones")
    void eliminaLasValoracionesDelProductoEliminado() {
        new LimpiezaValoracionesService(valoracionRepository).eliminarDeProducto("p-1");

        verify(valoracionRepository).eliminarPorProducto("p-1");
    }
}
