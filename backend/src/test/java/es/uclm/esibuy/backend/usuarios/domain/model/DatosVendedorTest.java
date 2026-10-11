package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos propios del vendedor (T241): nombre comercial, categoría principal, DNI y teléfono opcional.
class DatosVendedorTest {

    private static final Dni DNI = new Dni("12345678Z");

    @Test
    @DisplayName("HU1-CP19: crea unos datos de vendedor válidos")
    void creaDatosVendedorValidos() {
        DatosVendedor datos = new DatosVendedor("Tienda Luis", "cat-1", DNI, new Telefono("612345678"));

        assertEquals("Tienda Luis", datos.nombreComercial());
        assertEquals("cat-1", datos.categoriaPrincipalId());
        assertEquals(DNI, datos.dni());
        assertEquals(new Telefono("612345678"), datos.telefono());
    }

    @Test
    @DisplayName("HU1-CP19: el teléfono del vendedor es opcional")
    void permiteVendedorSinTelefono() {
        assertDoesNotThrow(() -> new DatosVendedor("Tienda Luis", "cat-1", DNI, null));
    }
}
