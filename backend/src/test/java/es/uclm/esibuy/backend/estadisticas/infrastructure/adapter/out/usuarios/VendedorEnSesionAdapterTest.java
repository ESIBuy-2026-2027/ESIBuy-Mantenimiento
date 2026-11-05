package es.uclm.esibuy.backend.estadisticas.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

// Adaptador de estadísticas hacia el vendedor en sesión (HU17, T279), sin Spring
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU17-CP13, propuesto.
@ExtendWith(MockitoExtension.class)
class VendedorEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU17-CP13: el id del vendedor en sesión se pide al módulo de usuarios con su correo")
    void elIdDelVendedorSePideAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("vendedor@esibuy.test")).thenReturn("v-1");

        assertEquals("v-1", new VendedorEnSesionAdapter(autenticacion).idPorCorreo("vendedor@esibuy.test"));
    }
}
