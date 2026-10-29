package es.uclm.esibuy.backend.pedidos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de pedidos hacia el módulo de usuarios (HU10), sin Spring
@ExtendWith(MockitoExtension.class)
class ClienteEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU10-CP18: el id del cliente del pedido se pide al módulo de usuarios")
    void idPorCorreoPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new ClienteEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }
}
