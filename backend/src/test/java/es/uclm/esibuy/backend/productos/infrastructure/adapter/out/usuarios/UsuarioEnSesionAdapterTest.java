package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

// Adaptador de productos hacia el módulo de usuarios (HU32, T183/T184). El puerto de entrada de usuarios es
// un doble de prueba (Mockito): se prueba sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU32-CP01 a CP05, propuestos.
@ExtendWith(MockitoExtension.class)
class UsuarioEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU32-CP01: el id del vendedor se pide al módulo de usuarios por su puerto de entrada")
    void idPorCorreoPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("vendedor@esibuy.test")).thenReturn("v-1");

        assertEquals("v-1", new UsuarioEnSesionAdapter(autenticacion).idPorCorreo("vendedor@esibuy.test"));
    }
}
