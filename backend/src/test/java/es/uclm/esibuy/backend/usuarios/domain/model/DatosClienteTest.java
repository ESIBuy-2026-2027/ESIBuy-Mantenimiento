package es.uclm.esibuy.backend.usuarios.domain.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import es.uclm.esibuy.backend.usuarios.domain.exception.UsuarioInvalidoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

// Datos propios del rol cliente (AB#210, «Planteamiento inicial BBDD» y MVP del Sprint 1).
class DatosClienteTest {

    @Test
    @DisplayName("HU1-CP11: los datos de cliente tienen rol CLIENTE y la fecha de nacimiento es opcional")
    void creaDatosDeClienteSinFechaDeNacimiento() {
        // La fecha de nacimiento será obligatoria cuando el registro la pida (task de registro).
        DatosCliente datos = new DatosCliente(TipoCliente.PREMIUM, null);

        assertEquals(Rol.CLIENTE, datos.rol());
        assertEquals(TipoCliente.PREMIUM, datos.tipoCliente());
        assertNull(datos.fechaNacimiento());
    }

    @Test
    @DisplayName("HU1-CP11: rechaza un cliente sin tipo de cliente")
    void rechazaClienteSinTipoDeCliente() {
        assertThrows(UsuarioInvalidoException.class, () -> new DatosCliente(null, null));
    }
}
