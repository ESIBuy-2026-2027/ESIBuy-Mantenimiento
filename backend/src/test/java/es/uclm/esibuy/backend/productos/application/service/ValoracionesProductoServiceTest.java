package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.verify;

// Media y número de valoraciones de los productos (HU13, T415): los calcula el módulo de valoraciones y este caso de
// uso los guarda en el producto. El repositorio es un doble de prueba (Mockito): la actualización en MongoDB se prueba
// en ProductoMongoAdapterTest.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP27, propuesto.
@ExtendWith(MockitoExtension.class)
class ValoracionesProductoServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU13-CP27: actualizar la valoración de un producto guarda su media y su número de valoraciones")
    void actualizarLaValoracionGuardaLaMediaYElNumero() {
        ValoracionesProductoService servicio = new ValoracionesProductoService(productoRepository);

        servicio.actualizarValoracion("p-1", 4.5, 2);

        verify(productoRepository).actualizarValoracion("p-1", 4.5, 2);
    }
}
