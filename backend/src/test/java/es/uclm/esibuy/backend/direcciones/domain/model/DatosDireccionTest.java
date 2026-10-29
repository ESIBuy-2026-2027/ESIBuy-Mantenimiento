package es.uclm.esibuy.backend.direcciones.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.direcciones.domain.exception.DireccionInvalidaException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatosDireccionTest {

    private static final CodigoPostal CODIGO_POSTAL = new CodigoPostal("13071");
    private static final TelefonoContacto TELEFONO = new TelefonoContacto("612345678");

    private static DatosDireccion datos(String destinatario, String calle, String detalles, String ciudad,
            String provincia) {
        return new DatosDireccion(destinatario, calle, detalles, CODIGO_POSTAL, ciudad, provincia, TELEFONO);
    }

    @Test
    @DisplayName("HU5-CP03: una dirección completa conserva todos sus datos")
    void creaDireccionCompleta() {
        DatosDireccion direccion = datos("Juan Pérez", "Calle Mayor 1", "2º B", "Ciudad Real", "Ciudad Real");

        assertEquals("Juan Pérez", direccion.destinatario());
        assertEquals("Calle Mayor 1", direccion.calle());
        assertEquals("2º B", direccion.detalles());
        assertEquals(CODIGO_POSTAL, direccion.codigoPostal());
        assertEquals("Ciudad Real", direccion.ciudad());
        assertEquals("Ciudad Real", direccion.provincia());
        assertEquals(TELEFONO, direccion.telefono());
    }

    @Test
    @DisplayName("HU5-CP03: los detalles son opcionales y unos vacíos se guardan como ausentes")
    void losDetallesSonOpcionales() {
        assertNull(datos("Juan Pérez", "Calle Mayor 1", null, "Ciudad Real", "Ciudad Real").detalles());
        assertNull(datos("Juan Pérez", "Calle Mayor 1", "  ", "Ciudad Real", "Ciudad Real").detalles());
    }

    @Test
    @DisplayName("HU5-CP03: rechaza una dirección sin destinatario, calle, ciudad o provincia")
    void rechazaDireccionSinCamposObligatorios() {
        assertThrows(DireccionInvalidaException.class, () -> datos(null, "Calle Mayor 1", null, "Ciudad Real", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", " ", null, "Ciudad Real", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "Calle Mayor 1", null, "", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "Calle Mayor 1", null, "Ciudad Real", null));
    }

    @Test
    @DisplayName("HU5-CP03: rechaza una dirección sin código postal o sin teléfono")
    void rechazaDireccionSinCodigoPostalOTelefono() {
        assertThrows(DireccionInvalidaException.class, () -> new DatosDireccion("Juan Pérez", "Calle Mayor 1", null,
                null, "Ciudad Real", "Ciudad Real", TELEFONO));
        assertThrows(DireccionInvalidaException.class, () -> new DatosDireccion("Juan Pérez", "Calle Mayor 1", null,
                CODIGO_POSTAL, "Ciudad Real", "Ciudad Real", null));
    }

    @Test
    @DisplayName("HU5-CP03: acepta cada campo con su longitud máxima y rechaza un carácter más")
    void respetaLaLongitudMaximaDeCadaCampo() {
        // Fronteras: 100 caracteres en destinatario, detalles, ciudad y provincia, y 150 en la calle
        String cien = "a".repeat(100);
        String cientoCincuenta = "a".repeat(150);
        datos(cien, cientoCincuenta, cien, cien, cien);

        String cientoUno = "a".repeat(101);
        assertThrows(DireccionInvalidaException.class, () -> datos(cientoUno, "Calle Mayor 1", null, "Ciudad Real", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "a".repeat(151), null, "Ciudad Real", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "Calle Mayor 1", cientoUno, "Ciudad Real", "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "Calle Mayor 1", null, cientoUno, "Ciudad Real"));
        assertThrows(DireccionInvalidaException.class, () -> datos("Juan Pérez", "Calle Mayor 1", null, "Ciudad Real", cientoUno));
    }
}
