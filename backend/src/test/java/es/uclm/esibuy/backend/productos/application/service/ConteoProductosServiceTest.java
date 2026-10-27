package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;

// Recuento de productos por categoría (HU39, T332), que usa el listado de categorías del administrador.
// El repositorio es un doble de prueba: se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU39-CP02, propuesto.
@ExtendWith(MockitoExtension.class)
class ConteoProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @InjectMocks
    private ConteoProductosService servicio;

    @Test
    @DisplayName("HU39-CP02: el recuento por categoría es el del repositorio")
    void contarPorCategoriaDelegaEnElRepositorio() {
        Map<String, Long> recuento = Map.of("cat-1", 2L, "cat-2", 1L);
        when(productoRepository.contarPorCategoria()).thenReturn(recuento);

        assertEquals(recuento, servicio.contarPorCategoria());
    }
}
