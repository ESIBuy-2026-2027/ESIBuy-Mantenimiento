package es.uclm.esibuy.backend.usuarios.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.out.UsuarioRepositoryPort;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.DatosPersonales;
import es.uclm.esibuy.backend.usuarios.domain.model.Dni;
import es.uclm.esibuy.backend.usuarios.domain.model.Email;
import es.uclm.esibuy.backend.usuarios.domain.model.Rol;
import es.uclm.esibuy.backend.usuarios.domain.model.TipoCliente;
import es.uclm.esibuy.backend.usuarios.domain.model.Usuario;

// Qué clientes son premium, para que las alertas de bajada de precio (AB#529) comparen el precio que paga cada uno.
// El repositorio es un doble de prueba: sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class TipoClienteServiceTest {

    private static final DatosPersonales DATOS_PERSONALES = new DatosPersonales("Ana", "López", null);

    @Mock
    private UsuarioRepositoryPort usuarioRepository;
    @InjectMocks
    private TipoClienteService servicio;

    private static Usuario cliente(String id, String correo, TipoCliente tipo) {
        return new Usuario(id, DATOS_PERSONALES, new Email(correo), "hash", Rol.CLIENTE,
                new DatosCliente(tipo, new Dni("12345678Z"), LocalDate.of(1996, 5, 20), null), null);
    }

    @Test
    @DisplayName("HU529-CP13: de los clientes indicados, devuelve solo los premium; los que no existen no lo son")
    void devuelveSoloLosPremium() {
        when(usuarioRepository.buscarPorId("c-premium"))
                .thenReturn(Optional.of(cliente("c-premium", "ana@uclm.es", TipoCliente.PREMIUM)));
        when(usuarioRepository.buscarPorId("c-estandar"))
                .thenReturn(Optional.of(cliente("c-estandar", "juan@uclm.es", TipoCliente.ESTANDAR)));
        when(usuarioRepository.buscarPorId("c-borrado")).thenReturn(Optional.empty());

        assertEquals(Set.of("c-premium"), servicio.premiumEntre(List.of("c-premium", "c-estandar", "c-borrado")));
    }

    @Test
    @DisplayName("HU529-CP13: sin clientes no consulta nada")
    void sinClientesNoConsultaNada() {
        assertTrue(servicio.premiumEntre(List.of()).isEmpty());
        verifyNoInteractions(usuarioRepository);
    }
}
