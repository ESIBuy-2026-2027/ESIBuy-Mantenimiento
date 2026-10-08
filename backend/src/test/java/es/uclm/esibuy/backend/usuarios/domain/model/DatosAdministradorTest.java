package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import java.time.LocalDate;
import java.time.Month;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos propios del rol administrador (AB#210, «Planteamiento inicial BBDD» y MVP del Sprint 1).
class DatosAdministradorTest {

    private static final LocalDate INCORPORACION = LocalDate.of(2026, Month.SEPTEMBER, 1);

    @Test
    @DisplayName("HU1-CP11: los datos de administrador tienen rol ADMINISTRADOR, sede y fecha de incorporación")
    void creaDatosDeAdministrador() {
        DatosAdministrador datos = new DatosAdministrador("Ciudad Real", INCORPORACION);

        assertEquals(Rol.ADMINISTRADOR, datos.rol());
        assertEquals("Ciudad Real", datos.sede());
        assertEquals(INCORPORACION, datos.fechaIncorporacion());
    }

    @Test
    @DisplayName("HU1-CP11: la sede del administrador es opcional")
    void permiteAdministradorSinSede() {
        assertNull(new DatosAdministrador(null, INCORPORACION).sede());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza una sede en blanco")
    void rechazaSedeEnBlanco() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosAdministrador("   ", INCORPORACION));
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un administrador sin fecha de incorporación")
    void rechazaAdministradorSinFechaDeIncorporacion() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosAdministrador("Ciudad Real", null));
    }
}
