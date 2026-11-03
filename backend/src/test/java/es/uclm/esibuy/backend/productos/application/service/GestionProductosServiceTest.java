package es.uclm.esibuy.backend.productos.application.service;

import es.uclm.esibuy.backend.productos.application.port.in.CrearProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.in.ModificarProductoCommand;
import es.uclm.esibuy.backend.productos.application.port.out.AnalisisImagenPort;
import es.uclm.esibuy.backend.productos.application.port.out.ProductoRepositoryPort;
import es.uclm.esibuy.backend.productos.application.port.out.ResultadoAnalisis;
import es.uclm.esibuy.backend.productos.application.port.out.UsuarioEnSesionPort;
import es.uclm.esibuy.backend.productos.domain.exception.ImagenNoSeguraException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoDuplicadoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import es.uclm.esibuy.backend.productos.domain.exception.ProductoNoEncontradoException;
import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

// Alta (HU31, T204), consulta (HU33, T349), modificación (HU34, T343) eliminación (HU35, T346) y activación y desactivación (HU16, T284 y T285) de los productos propios del vendedor. El repositorio y el puerto hacia usuarios son dobles de
// prueba (Mockito): se prueba el caso de uso sin MongoDB ni Spring. El antivirus de las imágenes (HU31, T397) también
// es un doble: por defecto da la imagen por segura.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP13 a CP16, propuestos a partir del contrato del frontend, y
// HU31-CP32, CP33 y HU34-CP19, propuestos.
@ExtendWith(MockitoExtension.class)
class GestionProductosServiceTest {

    private static final String CORREO = "vendedor@esibuy.test";

    @Mock
    private ProductoRepositoryPort productoRepository;

    @Mock
    private UsuarioEnSesionPort usuarioEnSesion;

    @Mock
    private AnalisisImagenPort analisisImagen;

    private GestionProductosService servicio;

    @BeforeEach
    void preparar() {
        servicio = new GestionProductosService(productoRepository, usuarioEnSesion, analisisImagen);
        // Por defecto el antivirus da la imagen por buena: los tests que no tratan del análisis no dependen de él
        lenient().when(analisisImagen.analizar(any())).thenReturn(ResultadoAnalisis.SEGURA);
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
    @DisplayName("HU31-CP32: un producto con una imagen que el antivirus marca como maliciosa se rechaza y no se guarda")
    void rechazaUnaImagenMaliciosaYNoGuardaNada() {
        lenient().when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(analisisImagen.analizar("https://esibuy.test/teclado.png")).thenReturn(ResultadoAnalisis.MALICIOSA);

        CrearProductoCommand comando = comando(new BigDecimal("89.50"), 12, "cat-1");
        assertThrows(ImagenNoSeguraException.class, () -> servicio.crear(comando));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU31-CP33: si el antivirus no da resultado, el producto se guarda sin imagen")
    void sinResultadoDelAntivirusSeGuardaSinImagen() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(analisisImagen.analizar("https://esibuy.test/teclado.png")).thenReturn(ResultadoAnalisis.SIN_RESULTADO);
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(comando(new BigDecimal("89.50"), 12, "cat-1"));

        assertEquals("", productoGuardado().getImagen());
    }

    @Test
    @DisplayName("HU31-CP33: si el antivirus da la imagen por segura, el producto se guarda con ella")
    void unaImagenSeguraSeGuardaConElProducto() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(analisisImagen.analizar("https://esibuy.test/teclado.png")).thenReturn(ResultadoAnalisis.SEGURA);
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(comando(new BigDecimal("89.50"), 12, "cat-1"));

        assertEquals("https://esibuy.test/teclado.png", productoGuardado().getImagen());
    }

    @Test
    @DisplayName("HU31-CP33: un producto sin imagen no se envía al antivirus")
    void unProductoSinImagenNoSeAnaliza() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.crear(new CrearProductoCommand(CORREO, "Teclado mecánico", "Con cable", new BigDecimal("89.50"),
                12, "cat-1", null, true, Descuento.sinDescuento(), Descuento.sinDescuento()));

        verify(analisisImagen, never()).analizar(any());
        assertEquals("", productoGuardado().getImagen());
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

    @Test
    @DisplayName("HU34-CP01: guarda los cambios del producto, con el mismo id y el mismo vendedor, y devuelve el guardado")
    void guardaLosCambiosConElMismoIdYVendedor() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto resultado = servicio.modificarMiProducto(
                modificacion("p-9", "Teclado mecánico RGB", new BigDecimal("99.90")));

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardar(captor.capture());
        Producto guardado = captor.getValue();
        assertSame(guardado, resultado);
        assertEquals("p-9", guardado.getId());
        assertEquals("v-1", guardado.getVendedorId());
        assertEquals("Teclado mecánico RGB", guardado.getNombre());
        assertEquals("Inalámbrico", guardado.getDescripcion());
        assertEquals(new BigDecimal("99.90"), guardado.getPrecio());
        assertEquals(5, guardado.getStock());
        assertEquals("cat-2", guardado.getCategoriaId());
        assertEquals("https://esibuy.test/rgb.png", guardado.getImagen());
        assertFalse(guardado.isDisponible());
        assertEquals(new Descuento(true, new BigDecimal("20")), guardado.getDescuento());
        assertEquals(new Descuento(true, new BigDecimal("25")), guardado.getDescuentoPremium());
    }

    @Test
    @DisplayName("HU34-CP02: se aplican las mismas validaciones que en el alta y unos datos inválidos no se guardan")
    void rechazaUnaModificacionConDatosInvalidos() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));

        assertThrows(ProductoInvalidoException.class,
                () -> servicio.modificarMiProducto(modificacion("p-9", "Teclado mecánico RGB", BigDecimal.ZERO)));
        assertThrows(ProductoInvalidoException.class,
                () -> servicio.modificarMiProducto(modificacion("p-9", " ", new BigDecimal("99.90"))));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU34-CP03: no se puede cambiar el nombre por el de otro producto del vendedor y no se guarda nada")
    void rechazaCambiarElNombrePorUnoRepetido() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));
        when(productoRepository.existePorVendedorYNombre("v-1", "Ratón inalámbrico")).thenReturn(true);

        assertThrows(ProductoDuplicadoException.class,
                () -> servicio.modificarMiProducto(modificacion("p-9", "Ratón inalámbrico", new BigDecimal("99.90"))));

        verify(productoRepository, never()).guardar(any());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Teclado mecánico", "TECLADO MECÁNICO"})
    @DisplayName("HU34-CP04: conservar el nombre del producto, aunque cambien las mayúsculas, no es un nombre repetido")
    void conservarElNombreNoEsUnNombreRepetido(String nombre) {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));
        // El propio producto ya tiene ese nombre: la consulta al repositorio lo encontraría a él.
        lenient().when(productoRepository.existePorVendedorYNombre("v-1", nombre)).thenReturn(true);
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto resultado = servicio.modificarMiProducto(modificacion("p-9", nombre, new BigDecimal("99.90")));

        assertEquals(nombre, resultado.getNombre());
    }

    @Test
    @DisplayName("HU34-CP05: un producto que no existe no se puede modificar y no se guarda nada")
    void unProductoQueNoExisteNoSeModifica() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-404")).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.modificarMiProducto(modificacion("p-404", "Teclado mecánico RGB", new BigDecimal("99.90"))));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU34-CP05: un vendedor no puede modificar un producto de otro vendedor y no se guarda nada")
    void unProductoDeOtroVendedorNoSeModifica() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-2")));

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.modificarMiProducto(modificacion("p-9", "Teclado mecánico RGB", new BigDecimal("99.90"))));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU34-CP19: cambiar la imagen por una que el antivirus marca como maliciosa se rechaza y no se guarda")
    void cambiarAUnaImagenMaliciosaSeRechaza() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));
        when(analisisImagen.analizar("https://esibuy.test/rgb.png")).thenReturn(ResultadoAnalisis.MALICIOSA);

        ModificarProductoCommand cambios = modificacion("p-9", "Teclado mecánico", new BigDecimal("99.90"));
        assertThrows(ImagenNoSeguraException.class, () -> servicio.modificarMiProducto(cambios));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU34-CP19: si al cambiar la imagen el antivirus no da resultado, el producto se guarda sin imagen")
    void cambiarLaImagenSinResultadoSeGuardaSinImagen() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));
        when(analisisImagen.analizar("https://esibuy.test/rgb.png")).thenReturn(ResultadoAnalisis.SIN_RESULTADO);
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.modificarMiProducto(modificacion("p-9", "Teclado mecánico", new BigDecimal("99.90")));

        assertEquals("", productoGuardado().getImagen());
    }

    @Test
    @DisplayName("HU34-CP19: si la imagen no cambia no se vuelve a analizar y se conserva")
    void siLaImagenNoCambiaNoSeVuelveAAnalizar() {
        Producto conImagen = new Producto("p-9", "v-1", "Teclado mecánico", "Con cable", new BigDecimal("89.50"), 12,
                "cat-1", "https://esibuy.test/rgb.png", true, Descuento.sinDescuento(), Descuento.sinDescuento());
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(conImagen));
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        servicio.modificarMiProducto(modificacion("p-9", "Teclado mecánico", new BigDecimal("99.90")));

        verify(analisisImagen, never()).analizar(any());
        assertEquals("https://esibuy.test/rgb.png", productoGuardado().getImagen());
    }

    @Test
    @DisplayName("HU35-CP01: el vendedor elimina un producto suyo")
    void elVendedorEliminaSuProducto() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-1")));

        servicio.eliminarMiProducto(CORREO, "p-9");

        verify(productoRepository).eliminar("p-9");
    }

    @Test
    @DisplayName("HU35-CP02: un producto que no existe no se puede eliminar y no se borra nada")
    void unProductoQueNoExisteNoSeElimina() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-404")).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.eliminarMiProducto(CORREO, "p-404"));

        verify(productoRepository, never()).eliminar(any());
    }

    @Test
    @DisplayName("HU35-CP02: un vendedor no puede eliminar un producto de otro vendedor y no se borra nada")
    void unProductoDeOtroVendedorNoSeElimina() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-2")));

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.eliminarMiProducto(CORREO, "p-9"));

        verify(productoRepository, never()).eliminar(any());
    }

    @Test
    @DisplayName("HU16-CP07: el vendedor desactiva un producto suyo y el resto de sus datos no cambia")
    void elVendedorDesactivaSuProducto() {
        Producto actual = producto("p-9", "v-1");
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(actual));
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto devuelto = servicio.cambiarDisponibilidadMiProducto(CORREO, "p-9", false);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardar(captor.capture());
        Producto guardado = captor.getValue();
        assertFalse(guardado.isDisponible());
        assertEquals("p-9", guardado.getId());
        assertEquals("v-1", guardado.getVendedorId());
        assertEquals(actual.getNombre(), guardado.getNombre());
        assertEquals(actual.getDescripcion(), guardado.getDescripcion());
        assertEquals(actual.getPrecio(), guardado.getPrecio());
        assertEquals(actual.getStock(), guardado.getStock());
        assertEquals(actual.getCategoriaId(), guardado.getCategoriaId());
        assertEquals(actual.getImagen(), guardado.getImagen());
        assertEquals(actual.getDescuento(), guardado.getDescuento());
        assertEquals(actual.getDescuentoPremium(), guardado.getDescuentoPremium());
        assertSame(guardado, devuelto);
    }

    @Test
    @DisplayName("HU16-CP08: el vendedor activa un producto suyo que no estaba disponible")
    void elVendedorActivaSuProducto() {
        Producto noDisponible = producto("p-9", "v-1").conDisponibilidad(false);
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(noDisponible));
        when(productoRepository.guardar(any(Producto.class))).thenAnswer(invocacion -> invocacion.getArgument(0));

        Producto devuelto = servicio.cambiarDisponibilidadMiProducto(CORREO, "p-9", true);

        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardar(captor.capture());
        assertTrue(captor.getValue().isDisponible());
        assertEquals("p-9", captor.getValue().getId());
        assertSame(captor.getValue(), devuelto);
    }

    @Test
    @DisplayName("HU16-CP09: un producto que no existe no se puede activar ni desactivar y no se guarda nada")
    void unProductoQueNoExisteNoCambiaDeDisponibilidad() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-404")).thenReturn(Optional.empty());

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.cambiarDisponibilidadMiProducto(CORREO, "p-404", false));

        verify(productoRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU16-CP09: un vendedor no puede activar ni desactivar un producto de otro vendedor y no se guarda nada")
    void unProductoDeOtroVendedorNoCambiaDeDisponibilidad() {
        when(usuarioEnSesion.idPorCorreo(CORREO)).thenReturn("v-1");
        when(productoRepository.buscarPorId("p-9")).thenReturn(Optional.of(producto("p-9", "v-2")));

        assertThrows(ProductoNoEncontradoException.class,
                () -> servicio.cambiarDisponibilidadMiProducto(CORREO, "p-9", false));

        verify(productoRepository, never()).guardar(any());
    }

    private Producto productoGuardado() {
        ArgumentCaptor<Producto> captor = ArgumentCaptor.forClass(Producto.class);
        verify(productoRepository).guardar(captor.capture());
        return captor.getValue();
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

    private static ModificarProductoCommand modificacion(String productoId, String nombre, BigDecimal precio) {
        return new ModificarProductoCommand(CORREO, productoId, nombre, "Inalámbrico", precio, 5, "cat-2",
                "https://esibuy.test/rgb.png", false, new Descuento(true, new BigDecimal("20")),
                new Descuento(true, new BigDecimal("25")));
    }
}
