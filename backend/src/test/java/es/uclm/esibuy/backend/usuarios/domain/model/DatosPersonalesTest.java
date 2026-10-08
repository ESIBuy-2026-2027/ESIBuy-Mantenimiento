package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos personales comunes a los roles (AB#210). Todos son opcionales hasta que el registro los
// pida, pero si se informan no pueden estar en blanco. El formato del DNI y del teléfono lo
// validará la task de registro.
class DatosPersonalesTest {

    @Test
    @DisplayName("HU1-CP11: crea los datos personales con nombre, apellidos, DNI y teléfono")
    void creaDatosPersonales() {
        DatosPersonales datos = new DatosPersonales("Ana", "García López", "12345678Z", "612345678");

        assertEquals("Ana", datos.nombre());
        assertEquals("García López", datos.apellidos());
        assertEquals("12345678Z", datos.dni());
        assertEquals("612345678", datos.telefono());
    }

    @Test
    @DisplayName("HU1-CP11: permite unos datos personales vacíos (registro solo con email y contraseña)")
    void permiteDatosPersonalesVacios() {
        DatosPersonales datos = DatosPersonales.sinDatos();

        assertNull(datos.nombre());
        assertNull(datos.apellidos());
        assertNull(datos.dni());
        assertNull(datos.telefono());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un nombre en blanco")
    void rechazaNombreEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales("   ", null, null, null));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza unos apellidos en blanco")
    void rechazaApellidosEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales(null, "   ", null, null));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un DNI en blanco")
    void rechazaDniEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales(null, null, "   ", null));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un teléfono en blanco")
    void rechazaTelefonoEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosPersonales(null, null, null, "   "));
    }
}
