package es.uclm.esibuy.backend.incidencias.infrastructure.adapter.out.usuarios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.usuarios.application.port.in.AutenticacionUseCase;

// Adaptador de incidencias hacia el módulo de usuarios (HU44, T293), sin Spring. Vale para un cliente y para un
// vendedor: el puerto de usuarios que usa es el mismo para los dos roles.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU44-CP12, propuesto.
@ExtendWith(MockitoExtension.class)
class AutorEnSesionAdapterTest {

    @Mock
    private AutenticacionUseCase autenticacion;

    @Test
    @DisplayName("HU44-CP12: el id del cliente que reporta se pide al módulo de usuarios")
    void idPorCorreoDeUnClientePreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("cliente@esibuy.test")).thenReturn("c-1");

        assertEquals("c-1", new AutorEnSesionAdapter(autenticacion).idPorCorreo("cliente@esibuy.test"));
    }

    @Test
    @DisplayName("HU44-CP12: el id del vendedor que reporta se pide al módulo de usuarios")
    void idPorCorreoDeUnVendedorPreguntaAlModuloDeUsuarios() {
        when(autenticacion.consultarIdUsuarioEnSesion("vendedor@esibuy.test")).thenReturn("v-1");

        assertEquals("v-1", new AutorEnSesionAdapter(autenticacion).idPorCorreo("vendedor@esibuy.test"));
    }
}