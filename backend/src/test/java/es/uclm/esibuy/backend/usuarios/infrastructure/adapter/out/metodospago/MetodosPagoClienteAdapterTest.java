package es.uclm.esibuy.backend.usuarios.infrastructure.adapter.out.metodospago;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.metodospago.application.port.in.GestionMetodosPagoUseCase;

// El adaptador solo traduce el puerto de salida de usuarios al caso de uso del módulo metodospago: se comprueba con un
// doble de prueba de ese caso de uso, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU25-CP06, propuesto.
class MetodosPagoClienteAdapterTest {

    private static final String EMAIL = "lucia@esibuy.test";

    private final GestionMetodosPagoUseCase casoDeUso = mock(GestionMetodosPagoUseCase.class);
    private final MetodosPagoClienteAdapter adapter = new MetodosPagoClienteAdapter(casoDeUso);

    @Test
    @DisplayName("HU25-CP06: al eliminar la cuenta se borran los métodos de pago del cliente con su correo")
    void eliminarTodosDelegaEnElCasoDeUsoConElMismoCorreo() {
        adapter.eliminarTodos(EMAIL);

        verify(casoDeUso).eliminarTodos(EMAIL);
    }
}
