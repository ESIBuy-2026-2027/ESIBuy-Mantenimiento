package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

// Adaptador de las alertas hacia el módulo de usuarios para el cliente en sesión (AB#529), sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP16, propuesto.
@ExtendWith(MockitoExtension.class)
class ClienteEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU529-CP16: el id del cliente en sesión se pide al módulo de usuarios por su puerto de entrada")
    void preguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new ClienteEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }
}
