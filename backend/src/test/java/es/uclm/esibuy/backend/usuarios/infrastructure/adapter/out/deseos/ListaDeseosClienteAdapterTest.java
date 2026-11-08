package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.deseos;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.deseos.application.port.in.GestionListaDeseosUseCase;

// El adaptador solo traduce el puerto de salida de usuarios al caso de uso del módulo deseos: se comprueba con un
// doble de prueba de ese caso de uso, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU25-CP06, propuesto.
class ListaDeseosClienteAdapterTest {

    private static final String EMAIL = "lucia@esibuy.test";

    private final GestionListaDeseosUseCase casoDeUso = mock(GestionListaDeseosUseCase.class);
    private final ListaDeseosClienteAdapter adapter = new ListaDeseosClienteAdapter(casoDeUso);

    @Test
    @DisplayName("HU25-CP06: al eliminar la cuenta se borra la lista de deseos del cliente con su correo")
    void eliminarDelegaEnElCasoDeUsoConElMismoCorreo() {
        adapter.eliminar(EMAIL);

        verify(casoDeUso).eliminar(EMAIL);
    }
}
