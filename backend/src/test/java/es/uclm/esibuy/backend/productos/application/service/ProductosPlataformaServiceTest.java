package es.uclm.esibuy.backend.productos.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.productos.application.port.in.ProductoEstadistica;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.domain.model.DatosProducto;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;

@ExtendWith(MockitoExtension.class)
class ProductosPlataformaServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Test
    @DisplayName("HU520-CP02: devuelve los productos de toda la plataforma con sus medias y cantidades de valoraciones")
    void devuelveResumenDeValoracionesDeTodosLosProductos() {
        when(productoRepository.listarTodos()).thenReturn(List.of(
                producto("p-1", "Teclado", "v-1", 4.5, 2),
                producto("p-2", "Ratón", "v-2", null, 0)));

        List<ProductoEstadistica> productos = new ProductosPlataformaService(productoRepository).listar();

        assertEquals(List.of(
                new ProductoEstadistica("p-1", "Teclado", "v-1", 4.5, 2),
                new ProductoEstadistica("p-2", "Ratón", "v-2", null, 0)), productos);
    }

    private static Producto producto(String id, String nombre, String vendedorId, Double media, int numeroValoraciones) {
        DatosProducto datos = new DatosProducto(
                new DatosProducto.DatosBasicos(vendedorId, nombre, "", new BigDecimal("10"), 5, "cat-1", ""),
                new DatosProducto.Configuracion(true, Descuento.sinDescuento(), Descuento.sinDescuento()),
                new DatosProducto.Valoraciones(media, numeroValoraciones));

        return new Producto(id, datos);
    }
}
