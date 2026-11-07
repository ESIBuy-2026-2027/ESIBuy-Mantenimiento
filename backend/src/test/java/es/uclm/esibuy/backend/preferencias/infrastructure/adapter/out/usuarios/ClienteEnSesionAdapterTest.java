package es.uclm.esibuy.backend.preferencias.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

// Adaptador de las preferencias hacia el módulo de usuarios (HU46, T287), sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU46-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class ClienteEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU46-CP13: el id del cliente se pide al módulo de usuarios por su puerto de entrada")
    void idPorCorreoPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new ClienteEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }
}
