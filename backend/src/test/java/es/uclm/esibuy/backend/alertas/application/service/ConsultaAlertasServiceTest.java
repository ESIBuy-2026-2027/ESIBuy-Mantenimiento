package es.uclm.esibuy.backend.alertas.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.ClienteEnSesionPort;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;

// Consulta de sus alertas por parte del cliente en sesión (AB#529). Los puertos de salida son dobles de prueba.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP09, propuesto.
@ExtendWith(MockitoExtension.class)
class ConsultaAlertasServiceTest {

    private static final String CLIENTE = "cliente@esibuy.test";

    @Mock
    private AlertaRepositoryPort alertaRepository;
    @Mock
    private ClienteEnSesionPort clienteEnSesion;
    @InjectMocks
    private ConsultaAlertasService servicio;

    @Test
    @DisplayName("HU529-CP09: el cliente consulta las alertas de su cuenta, en el orden del repositorio (de la más reciente a la más antigua)")
    void elClienteConsultaSusAlertas() {
        List<Alerta> suyas = List.of(
                Alerta.disponibilidad("c-1", "p-2", "Ratón", Instant.parse("2026-11-26T10:00:00Z")),
                Alerta.disponibilidad("c-1", "p-1", "Teclado", Instant.parse("2026-11-25T10:00:00Z")));
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorCliente("c-1")).thenReturn(suyas);

        assertEquals(suyas, servicio.consultar(CLIENTE));
    }

    @Test
    @DisplayName("HU529-CP09: un cliente sin alertas recibe una lista vacía")
    void unClienteSinAlertasRecibeUnaListaVacia() {
        when(clienteEnSesion.idPorCorreo(CLIENTE)).thenReturn("c-1");
        when(alertaRepository.buscarPorCliente("c-1")).thenReturn(List.of());

        assertTrue(servicio.consultar(CLIENTE).isEmpty());
    }
}
