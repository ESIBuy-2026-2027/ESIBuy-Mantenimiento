package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.direcciones;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.direcciones.application.port.in.GestionDireccionesUseCase;

// El adaptador solo traduce el puerto de salida de usuarios al caso de uso del módulo direcciones: se comprueba con un
// doble de prueba de ese caso de uso, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU25-CP06, propuesto.
class DireccionesClienteAdapterTest {

    private static final String EMAIL = "lucia@esibuy.test";

    private final GestionDireccionesUseCase casoDeUso = mock(GestionDireccionesUseCase.class);
    private final DireccionesClienteAdapter adapter = new DireccionesClienteAdapter(casoDeUso);

    @Test
    @DisplayName("HU25-CP06: al eliminar la cuenta se borran las direcciones del cliente con su correo")
    void eliminarTodasDelegaEnElCasoDeUsoConElMismoCorreo() {
        adapter.eliminarTodas(EMAIL);

        verify(casoDeUso).eliminarTodas(EMAIL);
    }
}
