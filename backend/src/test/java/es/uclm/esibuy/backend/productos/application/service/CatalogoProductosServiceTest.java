package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.BuscarCatalogoQuery;
import es.uclm.esibuy.backend.productos.application.port.in.CriterioOrdenCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.DetalleProductoCatalogo;
import es.uclm.esibuy.backend.productos.application.port.in.ProductoComercial;
import es.uclm.esibuy.backend.productos.application.port.in.ResumenProducto;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.application.port.out.VendedorPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Catálogo de productos del cliente (HU7, T352). El repositorio es un doble de prueba (Mockito): se prueba
// el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU7-CP10 y HU8-CP09 a CP11, propuestos.
@ExtendWith(MockitoExtension.class)
class CatalogoProductosServiceTest {

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private VendedorPort vendedorPort;

    @Mock
    private UsuarioEnSesionPort usuarioEnSesion;

    @Test
    @DisplayName("HU7-CP10: busca en el catálogo con los criterios recibidos y devuelve la página del repositorio")
    void buscaEnElCatalogoConLosCriteriosRecibidos() {
        BuscarCatalogoQuery query = new BuscarCatalogoQuery("teclado", "cat-2", 4, CriterioOrdenCatalogo.VALORACION,
                true, 0, 10);
        ProductoPage pagina = new ProductoPage(List.of(), 0, 10, 0);
        when(productoRepository.buscarEnCatalogo(query)).thenReturn(pagina);

        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

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
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

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
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

        assertTrue(servicio.consultarComprable("p-1", false).isEmpty());
        assertTrue(servicio.consultarComprable("no-existe", false).isEmpty());
    }

    @Test
    @DisplayName("HU14-CP16: el resumen de un producto disponible lleva sus datos y el precio final para el cliente")
    void elResumenDeUnProductoDisponibleLlevaSusDatos() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(true)));
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

        assertEquals(new ResumenProducto("p-1", "Teclado", "https://esibuy.test/teclado.png", new BigDecimal("40.00"), 7, true),
                servicio.consultarResumen("p-1", true).orElseThrow());
        assertEquals(new BigDecimal("45.00"), servicio.consultarResumen("p-1", false).orElseThrow().precioFinal());
    }

    @Test
    @DisplayName("HU14-CP16: un producto desactivado también tiene resumen, marcado como no disponible")
    void unProductoDesactivadoTambienTieneResumen() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(false)));
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

        assertFalse(servicio.consultarResumen("p-1", false).orElseThrow().disponible());
    }

    @Test
    @DisplayName("HU14-CP16: un producto que no existe no tiene resumen")
    void unProductoQueNoExisteNoTieneResumen() {
        when(productoRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

        assertTrue(servicio.consultarResumen("no-existe", false).isEmpty());
    }

    // Detalle de un producto del catálogo (HU8, T356): sus datos, el vendedor con su nombre comercial y el precio
    // con el descuento que le toca al cliente. Este cliente es estándar: se le aplica el descuento general.
    @Test
    @DisplayName("HU8-CP09: el detalle de un producto disponible lleva sus datos y el nombre comercial del vendedor")
    void elDetalleLlevaSusDatosYElNombreComercialDelVendedor() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(true)));
        when(vendedorPort.nombreComercial("u-2")).thenReturn("TecnoVera");
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort, usuarioEnSesion);

        DetalleProductoCatalogo detalle = servicio.consultarDetalle("p-1", "cliente@esibuy.test");

        assertEquals(new DetalleProductoCatalogo("p-1", "Teclado", "Con cable", "https://esibuy.test/teclado.png",
                "cat-1", "u-2", "TecnoVera", new BigDecimal("50.00"), new BigDecimal("10"), new BigDecimal("45.00"), 7,
                true, null, 0), detalle);
    }

    @Test
    @DisplayName("HU13-CP26: el detalle de un producto lleva su media y su número de valoraciones")
    void elDetalleLlevaSuMediaYSuNumeroDeValoraciones() {
        Producto valorado = new Producto("p-1", "u-2", "Teclado", "Con cable", new BigDecimal("50.00"), 7, "cat-1",
                "https://esibuy.test/teclado.png", true, Descuento.sinDescuento(), Descuento.sinDescuento(), 4.5, 2);
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(valorado));
        when(vendedorPort.nombreComercial("u-2")).thenReturn("TecnoVera");
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort,
                usuarioEnSesion);

        DetalleProductoCatalogo detalle = servicio.consultarDetalle("p-1", "cliente@esibuy.test");

        assertEquals(4.5, detalle.valoracion());
        assertEquals(2, detalle.numValoraciones());
    }

    @Test
    @DisplayName("HU8-CP10: al cliente premium se le muestra el precio con su descuento premium")
    void alClientePremiumSeLeMuestraElPrecioConSuDescuento() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(true)));
        when(vendedorPort.nombreComercial("u-2")).thenReturn("TecnoVera");
        when(usuarioEnSesion.esClientePremium("premium@esibuy.test")).thenReturn(true);
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort,
                usuarioEnSesion);

        DetalleProductoCatalogo detalle = servicio.consultarDetalle("p-1", "premium@esibuy.test");

        assertEquals(new BigDecimal("50.00"), detalle.precio());
        assertEquals(new BigDecimal("20"), detalle.porcentajeDescuento());
        assertEquals(new BigDecimal("40.00"), detalle.precioFinal());
    }

    // El cliente no distingue un producto que no existe de uno que el vendedor ha desactivado: los dos son un 404.
    @Test
    @DisplayName("HU8-CP11: el detalle de un producto que no existe o no está disponible no se encuentra")
    void elDetalleDeUnProductoQueNoExisteONoEstaDisponibleNoSeEncuentra() {
        when(productoRepository.buscarPorId("p-1")).thenReturn(Optional.of(producto(false)));
        when(productoRepository.buscarPorId("no-existe")).thenReturn(Optional.empty());
        CatalogoProductosService servicio = new CatalogoProductosService(productoRepository, vendedorPort,
                usuarioEnSesion);

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.consultarDetalle("p-1", "cliente@esibuy.test"));
        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.consultarDetalle("no-existe", "cliente@esibuy.test"));
    }
}
