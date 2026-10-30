package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.domain.model.ProductoPage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Catálogo de productos del cliente (HU7, T352). El repositorio es un doble de prueba (Mockito): se prueba
// el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU7-CP10 y HU8-CP09, propuestos.
@ExtendWith(MockitoExtension.class)
class CatalogoProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private VendedorPort vendedorPort;

    @Test
    @DisplayName("HU7-CP10: busca en el catálogo con los criterios recibidos y devuelve la página del repositorio")
    void buscaEnElCatalogoConLosCriteriosRecibidos() {
        BuscarCatalogoQuery query = new BuscarCatalogoQuery("teclado", "cat-2", 4, CriterioOrdenCatalogo.VALORACION,
                true, 0, 10);
        ProductoPage pagina = new ProductoPage(List.of(), 0, 10, 0);
        when(productoRepository.buscarEnCatalogo(query)).thenReturn(pagina);

        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort);

        assertSame(pagina, servicio.buscarProductos(query));
    }

    private static Producto producto(boolean disponible) {
        return new Producto("p-1", "u-2", "Teclado", "Con cable", new BigDecimal("50.00"), 7, "cat-1", "https://esibuy.test/teclado.png",
                disponible, new Descuento(true, new BigDecimal("10")), new Descuento(true, new BigDecimal("20")));
    }

    @Test
    @DisplayName("HU9-CP19: consultar un producto que se puede comprar devuelve sus datos y su precio final para el cliente")
    void consultarUnProductoComprableDevuelveSusDatosYSuPrecioFinal() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(true)));
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort);

        ProductoComercial comercial = servicio.consultarComprable("p-1", true).orElseThrow();

        assertEquals(new ProductoComercial("p-1", "u-2", "Teclado", "https://esibuy.test/teclado.png", new BigDecimal("40.00"), 7),
                comercial);
        assertEquals(new BigDecimal("45.00"), servicio.consultarComprable("p-1", false).orElseThrow().precioFinal());
    }

    @Test
    @DisplayName("HU9-CP12: un producto que no existe o no está disponible no se puede comprar")
    void unProductoQueNoExisteONoEstaDisponibleNoSePuedeComprar() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(false)));
        when(productoRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort);

        assertTrue(servicio.consultarComprable("p-1", false).isEmpty());
        assertTrue(servicio.consultarComprable("no-existe", false).isEmpty());
    }

    // Detalle de un producto del catálogo (HU8, T356): sus datos, el vendedor con su nombre comercial y el precio
    // con el descuento que le toca al cliente. Este cliente es estándar: se le aplica el descuento general.
    @Test
    @DisplayName("HU8-CP09: el detalle de un producto disponible lleva sus datos y el nombre comercial del vendedor")
    void elDetalleLlevaSusDatosYElNombreComercialDelVendedor() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(true)));
        when(vendedorPort.nombreComercial("u-2")).thenReturn("TecnoVera");
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort);

        DetalleProductoCatalogo detalle = servicio.consultarDetalle("p-1", "cliente@esibuy.test");

        assertEquals(new DetalleProductoCatalogo("p-1", "Teclado", "Con cable", "https://esibuy.test/teclado.png",
                "cat-1", "u-2", "TecnoVera", new BigDecimal("50.00"), new BigDecimal("10"), new BigDecimal("45.00"), 7,
                true), detalle);
    }
}
