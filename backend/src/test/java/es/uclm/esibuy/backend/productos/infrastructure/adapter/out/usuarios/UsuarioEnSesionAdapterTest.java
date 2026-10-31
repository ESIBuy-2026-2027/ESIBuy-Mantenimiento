package es.uclm.esibuy.backend.productos.infrastructure.adapter.out.usuarios;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

// Adaptador de productos hacia el módulo de usuarios (HU32, T183/T184). El puerto de entrada de usuarios es
// un doble de prueba (Mockito): se prueba sin Spring ni MongoDB.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU32-CP01 a CP05 y HU8-CP12, propuestos.
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

    @Test
    @DisplayName("HU8-CP12: si el cliente es premium se pregunta al módulo de usuarios por su puerto de entrada")
    void esClientePremiumPreguntaAlModuloDeUsuarios() {
        when(autenticacion.esClientePremium("premium@esibuy.test")).thenReturn(true);
        when(autenticacion.esClientePremium("estandar@esibuy.test")).thenReturn(false);

        UsuarioEnSesionAdapter adaptador = new UsuarioEnSesionAdapter(autenticacion);

        assertAll(
                () -> assertTrue(adaptador.esClientePremium("premium@esibuy.test")),
                () -> assertFalse(adaptador.esClientePremium("estandar@esibuy.test")));
    }
}
