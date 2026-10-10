package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class DatosClienteTest {

    private static final Dni DNI = new Dni("12345678Z");
    private static final LocalDate NACIMIENTO = LocalDate.of(1996, 5, 20);

    @Test
    @DisplayName("HU1-CP11: crea unos datos de cliente válidos, con el teléfono opcional")
    void creaDatosClienteValidos() {
        DatosCliente datos = new DatosCliente(TipoCliente.ESTANDAR, DNI, NACIMIENTO, null);

        assertEquals(TipoCliente.ESTANDAR, datos.tipoCliente());
        assertEquals(DNI, datos.dni());
        assertEquals(NACIMIENTO, datos.fechaNacimiento());
        assertNull(datos.telefono());
    }

    @Test
    @DisplayName("HU1-CP08: rechaza unos datos de cliente sin tipo de cliente")
    void rechazaSinTipoDeCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosCliente(null, DNI, NACIMIENTO, null));
    }

    @Test
    @DisplayName("HU1-CP08: rechaza unos datos de cliente sin DNI")
    void rechazaSinDni() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosCliente(TipoCliente.ESTANDAR, null, NACIMIENTO, null));
    }

    @Test
    @DisplayName("HU1-CP08: rechaza unos datos de cliente sin fecha de nacimiento")
    void rechazaSinFechaDeNacimiento() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosCliente(TipoCliente.ESTANDAR, DNI, null, null));
    }
}
