package es.uclm.esibuy.backend.deseos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de la lista de deseos hacia el módulo de usuarios (HU14), sin Spring ni MongoDB
@ExtendWith(MockitoExtension.class)
class ClienteEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU14-CP01: el id del cliente se pide al módulo de usuarios por su puerto de entrada")
    void idPorCorreoPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new ClienteEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }
}
