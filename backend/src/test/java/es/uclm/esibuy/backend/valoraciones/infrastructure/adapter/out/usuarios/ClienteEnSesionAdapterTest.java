package es.uclm.esibuy.backend.valoraciones.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de valoraciones hacia el módulo de usuarios (HU13, T370), sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU13-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class ClienteEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU13-CP13: el id del cliente que valora se pide al módulo de usuarios")
    void idPorCorreoPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new ClienteEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }
}
