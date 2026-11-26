package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.TipoClienteUseCase;

// Adaptador de las alertas hacia el módulo de usuarios para el tipo de cliente (AB#529), sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP16, propuesto.
@ExtendWith(MockitoExtension.class)
class TiposClienteAdapterTest {

    @Mock
    private TipoClienteUseCase tipoCliente;

    @Test
    @DisplayName("HU529-CP16: qué clientes son premium se pide al módulo de usuarios por su puerto de entrada")
    void preguntaAlModuloDeUsuarios() {
        when(tipoCliente.premiumEntre(List.of("c-1", "c-2"))).thenReturn(Set.of("c-2"));

        assertEquals(Set.of("c-2"), new TiposClienteAdapter(tipoCliente).premiumEntre(List.of("c-1", "c-2")));
    }
}
