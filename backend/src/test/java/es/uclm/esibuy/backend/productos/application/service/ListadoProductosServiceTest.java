package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.ListarProductosQuery;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.when;

// Listado de los productos propios del vendedor (HU32, T184). El repositorio y el puerto hacia usuarios son
// dobles de prueba (Mockito): se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU32-CP01 a CP05, propuestos.
@ExtendWith(MockitoExtension.class)
class ListadoProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private UsuarioEnSesionPort usuarioEnSesion;

    @Test
    @DisplayName("HU32-CP01: lista los productos del vendedor de la sesión, buscado por su correo")
    void listaLosProductosDelVendedorDeLaSesion() {
        ListarProductosQuery query = new ListarProductosQuery("teclado", "cat-2", 0, 10);
        ProductoPage pagina = new ProductoPage(List.of(), 0, 10, 0);
        when(usuarioEnSesion.idPorCorreo("vendedor@esibuy.test")).thenReturn("v-1");
        when(productoRepository.listarPorVendedor("v-1", query)).thenReturn(pagina);

        ListadoProductosService servicio = new ListadoProductosService(productoRepository, usuarioEnSesion);

        assertSame(pagina, servicio.listarMisProductos("vendedor@esibuy.test", query));
    }
}
