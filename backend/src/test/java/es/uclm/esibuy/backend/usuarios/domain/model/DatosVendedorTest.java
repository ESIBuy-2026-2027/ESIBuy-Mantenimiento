package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos propios del vendedor (T241): nombre comercial, categoría principal, DNI y teléfono opcional.
class DatosVendedorTest {

    private static final Dni DNI = new Dni("12345678Z");
    private static final String NOMBRE_DE_30 = "a".repeat(30);
    private static final String NOMBRE_DE_31 = "a".repeat(31);

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

    @Test
    @DisplayName("HU1-CP21: rechaza un vendedor sin nombre comercial")
    void rechazaSinNombreComercial() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor(null, "cat-1", DNI, null));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("   ", "cat-1", DNI, null));
    }

    @Test
    @DisplayName("HU1-CP21: rechaza un nombre comercial de 31 caracteres")
    void rechazaNombreComercialDe31Caracteres() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor(NOMBRE_DE_31, "cat-1", DNI, null));
    }

    @Test
    @DisplayName("HU1-CP21: acepta un nombre comercial de exactamente 30 caracteres")
    void aceptaNombreComercialDe30Caracteres() {
        // Frontera del máximo del formulario: 30 sí, 31 no
        assertDoesNotThrow(() -> new DatosVendedor(NOMBRE_DE_30, "cat-1", DNI, null));
    }

    @Test
    @DisplayName("HU1-CP21: rechaza un vendedor sin categoría principal")
    void rechazaSinCategoriaPrincipal() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("Tienda Luis", null, DNI, null));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("Tienda Luis", "", DNI, null));
    }

    @Test
    @DisplayName("HU1-CP21: rechaza un vendedor sin DNI")
    void rechazaSinDni() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("Tienda Luis", "cat-1", null, null));
    }
}
