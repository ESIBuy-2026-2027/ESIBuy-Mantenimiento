package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Alta de un producto por el vendedor (HU31, T204). El repositorio y el puerto hacia usuarios son dobles de
// prueba (Mockito): se prueba el caso de uso sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP13 a CP16, propuestos a partir del contrato del frontend.
@ExtendWith(MockitoExtension.class)
class GestionProductosServiceTest {

    private static final String CORREO = "vendedor@esibuy.test";

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private UsuarioEnSesionPort usuarioEnSesion;

    private GestionProductosService servicio;

    @BeforeEach
    void preparar() {
        servicio = new GestionProductosService(productoRepository, usuarioEnSesion);
    }

    @Test
    @DisplayName("HU31-CP13: crea el producto con el vendedor de la sesión y lo guarda")
    void creaElProductoConElVendedorDeLaSesion() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(comando(new BigDecimal("89.50"), 12, "cat-1"));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardar(captor.capture());
        Producto guardado = captor.getValue();
        assertNull(guardado.getId());
        assertEquals("v-1", guardado.getVendedorId());
        assertEquals("Teclado mecánico", guardado.getNombre());
        assertEquals("Con cable", guardado.getDescripcion());
        assertEquals(new BigDecimal("89.50"), guardado.getPrecio());
        assertEquals(12, guardado.getStock());
        assertEquals("cat-1", guardado.getCategoriaId());
        assertTrue(guardado.isDisponible());
        assertEquals(new Descuento(true, new BigDecimal("10")), guardado.getDescuento());
        assertEquals(Descuento.sinDescuento(), guardado.getDescuentoPremium());
    }

    @Test
    @DisplayName("HU31-CP14: devuelve el producto guardado, con el id que le ha asignado la base de datos")
    void devuelveElProductoGuardadoConSuId() {
        Producto conId = producto("p-9", "v-1");
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.guardar(any(Producto.class))).thenReturn(conId);

        Producto resultado = servicio.crear(comando(new BigDecimal("89.50"), 12, "cat-1"));

        assertSame(conId, resultado);
    }

    @Test
    @DisplayName("HU31-CP15: unos datos que incumplen las reglas del producto se rechazan y no se guarda nada")
    void rechazaDatosInvalidosYNoGuardaNada() {
        lenient().when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");

        assertThrows(ProductoInvalidoException.class,
                () -> servicio.crear(comando(BigDecimal.ZERO, 12, "cat-1")));
        assertThrows(ProductoInvalidoException.class,
                () -> servicio.crear(comando(new BigDecimal("89.50"), -1, "cat-1")));
        assertThrows(ProductoInvalidoException.class,
                () -> servicio.crear(comando(new BigDecimal("89.50"), 12, " ")));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU31-CP16: un vendedor no puede tener dos productos con el mismo nombre y no se guarda el segundo")
    void rechazaUnNombreRepetidoDelMismoVendedor() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.existePorVendedorYNombre("v-1", "Teclado mecánico")).thenReturn(true);

        assertThrows(ProductoDuplicadoException.class,
                () -> servicio.crear(comando(new BigDecimal("89.50"), 12, "cat-1")));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU33-CP01: devuelve el producto si pertenece al vendedor de la sesión")
    void devuelveElProductoSiEsDelVendedorDeLaSesion() {
        Producto propio = producto("p-9", "v-1");
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(propio));

        Producto resultado = servicio.consultarMiProducto(CORREO, "p-9");

        assertSame(propio, resultado);
    }

    @Test
    @DisplayName("HU33-CP02: un producto que no existe no se encuentra")
    void unProductoQueNoExisteNoSeEncuentra() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-404")).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.consultarMiProducto(CORREO, "p-404"));
    }

    @Test
    @DisplayName("HU33-CP03: un producto de otro vendedor no se encuentra, para no revelar que existe")
    void unProductoDeOtroVendedorNoSeEncuentra() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-2")));

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.consultarMiProducto(CORREO, "p-9"));
    }

    private static Producto producto(String id, String vendedorId) {
        return new Producto(id, vendedorId, "Teclado mecánico", "Con cable", new BigDecimal("89.50"), 12,
                "cat-1", "", true, Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    private static CrearProductoCommand comando(BigDecimal precio, int stock, String categoriaId) {
        return new CrearProductoCommand(CORREO, "Teclado mecánico", "Con cable", precio, stock, categoriaId,
                "https://esibuy.test/teclado.png", true, new Descuento(true, new BigDecimal("10")),
                Descuento.sinDescuento());
    }
}
