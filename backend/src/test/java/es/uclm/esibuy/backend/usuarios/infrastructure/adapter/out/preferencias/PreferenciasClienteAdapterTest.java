package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.preferencias;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.preferencias.application.port.in.GestionPreferenciasUseCase;

// El adaptador solo traduce el puerto de salida de usuarios al caso de uso del módulo preferencias: se comprueba con un
// doble de prueba de ese caso de uso, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU25-CP06, propuesto.
class PreferenciasClienteAdapterTest {

    private static final String EMAIL = "lucia@esibuy.test";

    private final GestionPreferenciasUseCase casoDeUso = mock(GestionPreferenciasUseCase.class);
    private final PreferenciasClienteAdapter adapter = new PreferenciasClienteAdapter(casoDeUso);

    @Test
    @DisplayName("HU25-CP06: al eliminar la cuenta se borran las preferencias del cliente con su correo")
    void eliminarDelegaEnElCasoDeUsoConElMismoCorreo() {
        adapter.eliminar(EMAIL);

        verify(casoDeUso).eliminar(EMAIL);
    }
}
