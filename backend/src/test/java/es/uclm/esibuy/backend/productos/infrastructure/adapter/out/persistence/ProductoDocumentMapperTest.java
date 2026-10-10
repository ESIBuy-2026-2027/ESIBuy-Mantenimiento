package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence;

import es.uclm.esibuy.backend.productos.domain.model.Descuento;
import es.uclm.esibuy.backend.productos.domain.model.Producto;
import es.uclm.esibuy.backend.productos.infrastructure.adapter.out.persistence.ProductoDocument.DescuentoDocument;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Conversión entre el producto del dominio y su documento de MongoDB (productos, HU31, T202).
// Sin Spring ni base de datos.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU31-CP11, propuesto.
class ProductoDocumentMapperTest {

    private final ProductoDocumentMapper mapper = new ProductoDocumentMapper();

    @Test
    @DisplayName("HU31-CP11: el documento guarda todos los datos del producto y sus dos descuentos")
    void convierteProductoEnDocumento() {
        Producto producto = new Producto("p-1", "v-1", "Teclado mecánico", "Switches rojos",
                new BigDecimal("89.50"), 12, "cat-2", "https://esibuy.test/teclado.png", true,
                new Descuento(true, new BigDecimal("10")), Descuento.sinDescuento());

        ProductoDocument documento = mapper.aDocumento(producto);

        assertEquals("p-1", documento.getId());
        assertEquals("v-1", documento.getVendedorId());
        assertEquals("Teclado mecánico", documento.getNombre());
        assertEquals("Switches rojos", documento.getDescripcion());
        assertEquals(new BigDecimal("89.50"), documento.getPrecio());
        assertEquals(12, documento.getStock());
        assertEquals("cat-2", documento.getCategoriaId());
        assertEquals("https://esibuy.test/teclado.png", documento.getImagen());
        assertTrue(documento.isDisponible());
        assertTrue(documento.getDescuento().isActivo());
        assertEquals(new BigDecimal("10"), documento.getDescuento().getPorcentaje());
        assertFalse(documento.getDescuentoPremium().isActivo());
        assertNull(documento.getDescuentoPremium().getPorcentaje());
    }

    @Test
    @DisplayName("HU31-CP11: un documento leído recupera el producto con su id y sus descuentos")
    void convierteDocumentoEnProducto() {
        ProductoDocument documento = new ProductoDocument();
        documento.setId("p-1");
        documento.setVendedorId("v-1");
        documento.setNombre("Teclado mecánico");
        documento.setDescripcion("Switches rojos");
        documento.setPrecio(new BigDecimal("89.50"));
        documento.setStock(12);
        documento.setCategoriaId("cat-2");
        documento.setImagen("");
        documento.setDisponible(false);
        documento.setDescuento(descuento(false, null));
        documento.setDescuentoPremium(descuento(true, new BigDecimal("15")));

        Producto producto = mapper.aDominio(documento);

        assertEquals("p-1", producto.getId());
        assertEquals("v-1", producto.getVendedorId());
        assertEquals("Teclado mecánico", producto.getNombre());
        assertEquals("Switches rojos", producto.getDescripcion());
        assertEquals(new BigDecimal("89.50"), producto.getPrecio());
        assertEquals(12, producto.getStock());
        assertEquals("cat-2", producto.getCategoriaId());
        assertEquals("", producto.getImagen());
        assertFalse(producto.isDisponible());
        assertEquals(Descuento.sinDescuento(), producto.getDescuento());
        assertEquals(new Descuento(true, new BigDecimal("15")), producto.getDescuentoPremium());
    }

    private static DescuentoDocument descuento(boolean activo, BigDecimal porcentaje) {
        DescuentoDocument descuento = new DescuentoDocument();
        descuento.setActivo(activo);
        descuento.setPorcentaje(porcentaje);
        return descuento;
    }
}
