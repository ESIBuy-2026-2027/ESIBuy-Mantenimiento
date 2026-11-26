package es.uclm.esibuy.backend.alertas.application.service;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.domain.exception.AlertaNoEncontradaException;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;

// Gestión de sus alertas por parte del cliente en sesión: marcarlas como leídas (AB#531). Los puertos de salida son
// dobles de prueba. PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU531-CP02, propuesto.
@ExtendWith(MockitoExtension.class)
class GestionAlertasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";
    private static final Instant HOY = Instant.parse("2026-11-26T10:00:00Z");

    @Mock
    private AlertaRepositoryPort alertaRepository;
    @Mock
    private ClienteEnSesionPort clienteEnSesion;
    @InjectMocks
    private GestionAlertasService servicio;

    @Test
    @DisplayName("HU531-CP02: el cliente marca una alerta suya como leída y se guarda leída")
    void elClienteMarcaUnaAlertaSuyaComoLeida() {
        Alerta nueva = new Alerta("a-1", "c-1", "p-1", TipoAlerta.DISPONIBILIDAD, HOY, false, null);
        Alerta guardada = nueva.marcarComoLeida();
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorIdYCliente("a-1", "c-1")).thenReturn(Optional.of(nueva));
        when(alertaRepository.guardar(any(Alerta.class))).thenReturn(guardada);

        Alerta resultado = servicio.marcarComoLeida(CLIENTE, "a-1");

        ArgumentCaptor<Alerta> captor = ArgumentCaptor.forClass(Alerta.class);
        verify(alertaRepository).guardar(captor.capture());
        assertTrue(captor.getValue().isLeida());
        assertSame(guardada, resultado);
    }

    @Test
    @DisplayName("HU531-CP02: marcar como leída una alerta que ya lo estaba no la vuelve a guardar")
    void unaAlertaYaLeidaNoSeVuelveAGuardar() {
        Alerta yaLeida = new Alerta("a-1", "c-1", "p-1", TipoAlerta.DISPONIBILIDAD, HOY, true, null);
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorIdYCliente("a-1", "c-1")).thenReturn(Optional.of(yaLeida));

        assertSame(yaLeida, servicio.marcarComoLeida(CLIENTE, "a-1"));
        verify(alertaRepository, never()).guardar(any());
    }

    @Test
    @DisplayName("HU531-CP02: una alerta que no existe o es de otro cliente no se encuentra y no se guarda nada")
    void unaAlertaInexistenteOAjenaNoSeEncuentra() {
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorIdYCliente("a-9", "c-1")).thenReturn(Optional.empty());

        assertThrows(AlertaNoEncontradaException.class, () -> servicio.marcarComoLeida(CLIENTE, "a-9"));
        verify(alertaRepository, never()).guardar(any());
    }
}
