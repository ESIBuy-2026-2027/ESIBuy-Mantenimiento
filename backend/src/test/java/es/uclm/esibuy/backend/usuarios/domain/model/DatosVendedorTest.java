package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos propios del rol vendedor (AB#210, «Planteamiento inicial BBDD» y MVP del Sprint 1).
class DatosVendedorTest {

    @Test
    @DisplayName("HU1-CP11: los datos de vendedor tienen rol VENDEDOR, nombre comercial y categoría principal")
    void creaDatosDeVendedor() {
        DatosVendedor datos = new DatosVendedor("Electrónica La Mancha", "cat-1");

        assertEquals(Rol.VENDEDOR, datos.rol());
        assertEquals("Electrónica La Mancha", datos.nombreComercial());
        assertEquals("cat-1", datos.categoriaPrincipalId());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un vendedor sin nombre comercial")
    void rechazaVendedorSinNombreComercial() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor(null, "cat-1"));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("   ", "cat-1"));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un vendedor sin categoría principal")
    void rechazaVendedorSinCategoriaPrincipal() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("Electrónica La Mancha", null));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosVendedor("Electrónica La Mancha", "   "));
    }
}
