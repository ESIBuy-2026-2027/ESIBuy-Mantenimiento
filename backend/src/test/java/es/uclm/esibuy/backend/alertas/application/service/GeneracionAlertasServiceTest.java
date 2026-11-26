package es.uclm.esibuy.backend.alertas.application.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto;
import es.uclm.esibuy.backend.alertas.application.port.in.CambioProducto.Situacion;
import es.uclm.esibuy.backend.alertas.application.port.out.AlertaRepositoryPort;
import es.uclm.esibuy.backend.alertas.application.port.out.FavoritosPort;
import es.uclm.esibuy.backend.alertas.application.port.out.TiposClientePort;
import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;

// Generación de las alertas de los productos favoritos (AB#529) cuando cambia un producto: una por cada cliente que
// lo tiene en favoritos. Los puertos de salida son dobles de prueba (Mockito): sin MongoDB ni Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU529-CP05 a CP08, propuestos.
@ExtendWith(MockitoExtension.class)
class GeneracionAlertasServiceTest {

    private static final Instant AHORA = Instant.parse("2026-11-26T10:00:00Z");
    private static final BigDecimal CIEN = new BigDecimal("100.00");
    private static final BigDecimal OCHENTA = new BigDecimal("80.00");
    private static final BigDecimal NOVENTA = new BigDecimal("90.00");

    @Mock
    private AlertaRepositoryPort alertaRepository;
    @Mock
    private FavoritosPort favoritos;
    @Mock
    private TiposClientePort tiposCliente;
    @Captor
    private ArgumentCaptor<List<Alerta>> guardadas;

    private GeneracionAlertasService servicio;

    @BeforeEach
    void preparar() {
        servicio = new GeneracionAlertasService(alertaRepository, favoritos, tiposCliente,
                Clock.fixed(AHORA, ZoneOffset.UTC));
    }

    private static CambioProducto cambio(Situacion antes, Situacion despues) {
        return new CambioProducto("p-1", "Teclado", antes, despues);
    }

    private List<Alerta> alertasGuardadas() {
        verify(alertaRepository).guardarTodas(guardadas.capture());
        return guardadas.getValue();
    }

    @Test
    @DisplayName("HU529-CP05: si baja el precio, cada cliente con el producto en favoritos recibe una alerta con su precio")
    void unaBajadaDePrecioAvisaACadaClienteConSuPrecio() {
        when(favoritos.clientesConFavorito("p-1")).thenReturn(List.of("c-estandar", "c-premium"));
        when(tiposCliente.premiumEntre(List.of("c-estandar", "c-premium"))).thenReturn(Set.of("c-premium"));

        servicio.notificarCambio(cambio(new Situacion(CIEN, NOVENTA, true), new Situacion(NOVENTA, OCHENTA, true)));

        List<Alerta> alertas = alertasGuardadas();
        assertEquals(2, alertas.size());
        Alerta estandar = alertas.get(0);
        Alerta premium = alertas.get(1);
        assertAll(
                () -> assertEquals("c-estandar", estandar.getClienteId()),
                () -> assertEquals(TipoAlerta.BAJADA_PRECIO, estandar.getTipo()),
                () -> assertEquals("p-1", estandar.getProductoId()),
                () -> assertEquals(AHORA, estandar.getFecha()),
                () -> assertFalse(estandar.isLeida()),
                () -> assertEquals("Teclado", estandar.getDetalle().nombreProducto()),
                () -> assertEquals(CIEN, estandar.getDetalle().precioAnterior()),
                () -> assertEquals(NOVENTA, estandar.getDetalle().precioNuevo()),
                () -> assertEquals("c-premium", premium.getClienteId()),
                () -> assertEquals(NOVENTA, premium.getDetalle().precioAnterior()),
                () -> assertEquals(OCHENTA, premium.getDetalle().precioNuevo()));
    }

    @Test
    @DisplayName("HU529-CP05: si solo baja el precio premium (p. ej. una oferta premium mayor), solo se avisa a los premium")
    void siSoloBajaElPrecioPremiumSoloSeAvisaALosPremium() {
        when(favoritos.clientesConFavorito("p-1")).thenReturn(List.of("c-estandar", "c-premium"));
        when(tiposCliente.premiumEntre(List.of("c-estandar", "c-premium"))).thenReturn(Set.of("c-premium"));

        servicio.notificarCambio(cambio(new Situacion(CIEN, NOVENTA, true), new Situacion(CIEN, OCHENTA, true)));

        List<Alerta> alertas = alertasGuardadas();
        assertEquals(1, alertas.size());
        assertEquals("c-premium", alertas.get(0).getClienteId());
    }

    @Test
    @DisplayName("HU529-CP06: si el precio sube o no cambia y la disponibilidad tampoco, no se genera ninguna alerta")
    void sinBajadaNiDisponibilidadNoHayAlertas() {
        servicio.notificarCambio(cambio(new Situacion(NOVENTA, OCHENTA, true), new Situacion(CIEN, NOVENTA, true)));
        servicio.notificarCambio(cambio(new Situacion(CIEN, NOVENTA, false), new Situacion(CIEN, NOVENTA, false)));

        verifyNoInteractions(favoritos, tiposCliente, alertaRepository);
    }

    @Test
    @DisplayName("HU529-CP07: si el producto vuelve a poder comprarse, cada cliente con él en favoritos recibe una alerta de disponibilidad")
    void siVuelveAEstarDisponibleSeAvisaATodos() {
        when(favoritos.clientesConFavorito("p-1")).thenReturn(List.of("c-1", "c-2"));

        servicio.notificarCambio(cambio(new Situacion(CIEN, CIEN, false), new Situacion(CIEN, CIEN, true)));

        List<Alerta> alertas = alertasGuardadas();
        assertEquals(2, alertas.size());
        assertEquals(TipoAlerta.DISPONIBILIDAD, alertas.get(0).getTipo());
        assertEquals("c-1", alertas.get(0).getClienteId());
        assertEquals("c-2", alertas.get(1).getClienteId());
        // Sin bajada de precio no hace falta saber quién es premium
        verifyNoInteractions(tiposCliente);
    }

    @Test
    @DisplayName("HU529-CP07: si a la vez vuelve a estar disponible y baja de precio, el cliente recibe las dos alertas")
    void siVuelveYBajaDePrecioHayDosAlertas() {
        when(favoritos.clientesConFavorito("p-1")).thenReturn(List.of("c-1"));
        when(tiposCliente.premiumEntre(List.of("c-1"))).thenReturn(Set.of());

        servicio.notificarCambio(cambio(new Situacion(CIEN, CIEN, false), new Situacion(NOVENTA, NOVENTA, true)));

        List<Alerta> alertas = alertasGuardadas();
        assertEquals(List.of(TipoAlerta.DISPONIBILIDAD, TipoAlerta.BAJADA_PRECIO),
                alertas.stream().map(Alerta::getTipo).toList());
    }

    @Test
    @DisplayName("HU529-CP08: si nadie tiene el producto en favoritos, no se guarda ninguna alerta")
    void sinClientesInteresadosNoSeGuardaNada() {
        when(favoritos.clientesConFavorito("p-1")).thenReturn(List.of());

        servicio.notificarCambio(cambio(new Situacion(CIEN, CIEN, true), new Situacion(NOVENTA, NOVENTA, true)));

        verify(alertaRepository, never()).guardarTodas(anyList());
        verify(tiposCliente, never()).premiumEntre(any());
    }
}
