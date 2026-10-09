package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatosPersonalesTest {

    private static final String NOMBRE_DE_30 = "Ana María de los Ángeles Pérez";   // 30 caracteres
    private static final String NOMBRE_DE_31 = NOMBRE_DE_30 + "x";

    @Test
    @DisplayName("HU1-CP11: crea unos datos personales válidos, con la imagen opcional")
    void creaDatosPersonalesValidos() {
        DatosPersonales datos = new DatosPersonales("Ana", "García López", null);

        assertEquals("Ana", datos.nombre());
        assertEquals("García López", datos.apellidos());
        assertNull(datos.imagenPerfil());
    }

    @Test
    @DisplayName("HU1-CP08: rechaza un nombre en blanco")
    void rechazaNombreEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales("   ", "García López", null));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales(null, "García López", null));
    }

    @Test
    @DisplayName("HU1-CP08: rechaza unos apellidos en blanco")
    void rechazaApellidosEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales("Ana", "   ", null));
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales("Ana", null, null));
    }

    @Test
    @DisplayName("HU1-CP08: rechaza un nombre de 31 caracteres")
    void rechazaNombreDe31Caracteres() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales(NOMBRE_DE_31, "García López", null));
    }

    @Test
    @DisplayName("HU1-CP08: acepta un nombre de exactamente 30 caracteres")
    void aceptaNombreDe30Caracteres() {
        // Frontera del máximo del Product Owner: 30 sí, 31 no (lo mismo aplica a los apellidos)
        assertDoesNotThrow(() -> new DatosPersonales(NOMBRE_DE_30, "García López", null));
    }
}
