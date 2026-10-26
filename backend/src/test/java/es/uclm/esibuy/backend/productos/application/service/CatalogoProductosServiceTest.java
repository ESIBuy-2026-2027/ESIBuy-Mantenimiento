package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

// Catálogo de productos del cliente (HU7, T352). El repositorio es un doble de prueba (Mockito): se prueba
// el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU7-CP10, propuesto.
@ExtendWith(MockitoExtension.class)
class CatalogoProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU7-CP10: busca en el catálogo con los criterios recibidos y devuelve la página del repositorio")
    void buscaEnElCatalogoConLosCriteriosRecibidos() {
        BuscarCatalogoQuery query = new BuscarCatalogoQuery("teclado", "cat-2", 4, CriterioOrdenCatalogo.VALORACION,
                true, 0, 10);
        ProductoPage pagina = new ProductoPage(List.of(), 0, 10, 0);
        when(productoRepository.buscarEnCatalogo(query)).thenReturn(pagina);

        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository);

        assertSame(pagina, servicio.buscarProductos(query));
    }
}
