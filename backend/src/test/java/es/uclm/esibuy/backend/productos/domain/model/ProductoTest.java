package es.uclm.esibuy.backend.productos.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;

import es.uclm.esibuy.backend.productos.domain.exception.ProductoInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

// Entidad Producto (HU31, T201). Las invariantes son las del contrato del frontend y las de la colección
// «productos»: vendedor y nombre obligatorios, precio > 0, stock >= 0, una sola categoría.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU31-CP01 a CP06 y CP09, propuestos a partir del formulario.
class ProductoTest {

    private static final BigDecimal PRECIO = new BigDecimal("89.50");

    @Test
    @DisplayName("HU31-CP01: crea un producto con datos correctos")
    void creaUnProductoConDatosCorrectos() {
        Descuento descuento = new Descuento(true, new BigDecimal("10"));
        Descuento premium = new Descuento(true, new BigDecimal("15"));

        Producto producto = new Producto("p-1", "u-2", "Teclado mecánico", "Con cable", PRECIO, 12, "cat-1",
                "https://esibuy.test/teclado.png", true, descuento, premium);

        assertEquals("p-1", producto.getId());
        assertEquals("u-2", producto.getVendedorId());
        assertEquals("Teclado mecánico", producto.getNombre());
        assertEquals("Con cable", producto.getDescripcion());
        assertEquals(PRECIO, producto.getPrecio());
        assertEquals(12, producto.getStock());
        assertEquals("cat-1", producto.getCategoriaId());
        assertEquals("https://esibuy.test/teclado.png", producto.getImagen());
        assertTrue(producto.isDisponible());
        assertEquals(descuento, producto.getDescuento());
        assertEquals(premium, producto.getDescuentoPremium());
    }

    @Test
    @DisplayName("HU31-CP01: permite un producto sin id antes de persistirlo")
    void permiteUnProductoSinIdAntesDePersistir() {
        assertNull(productoConNombre("Teclado").getId());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU31-CP02: rechaza un producto sin vendedor")
    void rechazaUnProductoSinVendedor(String vendedorId) {
        assertThrows(ProductoInvalidoException.class, () -> new Producto(null, vendedorId, "Teclado", "", PRECIO, 5,
                "cat-1", "", true, Descuento.sinDescuento(), Descuento.sinDescuento()));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU31-CP03: rechaza un producto sin nombre")
    void rechazaUnProductoSinNombre(String nombre) {
        assertThrows(ProductoInvalidoException.class, () -> productoConNombre(nombre));
    }

    @Test
    @DisplayName("HU31-CP03: el nombre admite hasta 100 caracteres")
    void elNombreTieneUnMaximoDe100Caracteres() {
        assertDoesNotThrow(() -> productoConNombre("a".repeat(100)));
        assertThrows(ProductoInvalidoException.class, () -> productoConNombre("a".repeat(101)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "0.00", "-1", "-0.01"})
    @DisplayName("HU31-CP04: rechaza un precio que no es mayor que 0")
    void rechazaUnPrecioNoPositivo(String precio) {
        assertThrows(ProductoInvalidoException.class, () -> productoConPrecio(new BigDecimal(precio)));
    }

    @Test
    @DisplayName("HU31-CP04: rechaza un precio nulo y acepta el mínimo positivo")
    void rechazaUnPrecioNuloYAceptaElMinimo() {
        assertThrows(ProductoInvalidoException.class, () -> productoConPrecio(null));
        assertDoesNotThrow(() -> productoConPrecio(new BigDecimal("0.01")));
    }

    @Test
    @DisplayName("HU31-CP05: rechaza un stock negativo y acepta cero")
    void rechazaUnStockNegativoYAceptaCero() {
        assertThrows(ProductoInvalidoException.class, () -> productoConStock(-1));
        assertDoesNotThrow(() -> productoConStock(0));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    @DisplayName("HU31-CP06: rechaza un producto sin categoría")
    void rechazaUnProductoSinCategoria(String categoriaId) {
        assertThrows(ProductoInvalidoException.class, () -> new Producto(null, "u-2", "Teclado", "", PRECIO, 5,
                categoriaId, "", true, Descuento.sinDescuento(), Descuento.sinDescuento()));
    }

    @Test
    @DisplayName("HU31-CP07: rechaza un producto sin descuento o sin descuento premium")
    void rechazaUnProductoSinDescuentos() {
        assertThrows(ProductoInvalidoException.class, () -> new Producto(null, "u-2", "Teclado", "", PRECIO, 5,
                "cat-1", "", true, null, Descuento.sinDescuento()));
        assertThrows(ProductoInvalidoException.class, () -> new Producto(null, "u-2", "Teclado", "", PRECIO, 5,
                "cat-1", "", true, Descuento.sinDescuento(), null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://esibuy.test/a.png", "http://esibuy.test/a.png", ""})
    @DisplayName("HU31-CP09: la imagen es opcional y, si existe, es una dirección http o https")
    void aceptaImagenesHttpOVacias(String imagen) {
        assertDoesNotThrow(() -> productoConImagen(imagen));
        assertDoesNotThrow(() -> productoConImagen(null));
    }

    @ParameterizedTest
    @ValueSource(strings = {"javascript:alert(1)", "ftp://esibuy.test/a.png", "no es una url", "data:text/html;base64,AAAA"})
    @DisplayName("HU31-CP09: rechaza una imagen que no sea una dirección http o https")
    void rechazaImagenesQueNoSonHttp(String imagen) {
        assertThrows(ProductoInvalidoException.class, () -> productoConImagen(imagen));
    }

    @Test
    @DisplayName("HU31-CP10: la descripción es opcional y admite hasta 1000 caracteres")
    void laDescripcionTieneUnMaximoDe1000Caracteres() {
        assertDoesNotThrow(() -> productoConDescripcion(null));
        assertDoesNotThrow(() -> productoConDescripcion("a".repeat(1000)));
        assertThrows(ProductoInvalidoException.class, () -> productoConDescripcion("a".repeat(1001)));
    }

    private static Producto productoConNombre(String nombre) {
        return new Producto(null, "u-2", nombre, "", PRECIO, 5, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    private static Producto productoConPrecio(BigDecimal precio) {
        return new Producto(null, "u-2", "Teclado", "", precio, 5, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    private static Producto productoConStock(int stock) {
        return new Producto(null, "u-2", "Teclado", "", PRECIO, stock, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    private static Producto productoConImagen(String imagen) {
        return new Producto(null, "u-2", "Teclado", "", PRECIO, 5, "cat-1", imagen, true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }

    private static Producto productoConDescripcion(String descripcion) {
        return new Producto(null, "u-2", "Teclado", descripcion, PRECIO, 5, "cat-1", "", true,
                Descuento.sinDescuento(), Descuento.sinDescuento());
    }
}