package es.uclm.esibuy.backend.alertas.domain.model;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.alertas.domain.exception.AlertaInvalidaException;

// Reglas de las alertas de los productos favoritos del cliente (AB#529): bajada de precio solo si el precio nuevo es
// menor, y disponibilidad cuando el producto pasa a poder comprarse. Java puro, sin Spring.
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: casos HU529-CP01 a CP04, propuestos.
class AlertaTest {

    private static final Instant AHORA = Instant.parse("2026-11-26T10:00:00Z");

    @Test
    @DisplayName("HU529-CP01: una bajada de precio crea una alerta no leída con el cliente, el producto, la fecha y los dos precios")
    void unaBajadaDePrecioCreaLaAlerta() {
        Alerta alerta = Alerta.bajadaDePrecio("c-1", "p-1", "Teclado", new BigDecimal("50.00"),
                new BigDecimal("40.00"), AHORA);

        assertAll(
                () -> assertNull(alerta.getId()),
                () -> assertEquals("c-1", alerta.getClienteId()),
                () -> assertEquals("p-1", alerta.getProductoId()),
                () -> assertEquals(TipoAlerta.BAJADA_PRECIO, alerta.getTipo()),
                () -> assertEquals(AHORA, alerta.getFecha()),
                () -> assertFalse(alerta.isLeida()),
                () -> assertEquals("Teclado", alerta.getDetalle().nombreProducto()),
                () -> assertEquals(new BigDecimal("50.00"), alerta.getDetalle().precioAnterior()),
                () -> assertEquals(new BigDecimal("40.00"), alerta.getDetalle().precioNuevo()));
    }

    @Test
    @DisplayName("HU529-CP02: solo es una bajada de precio si el precio nuevo es menor que el anterior")
    void soloEsBajadaSiElPrecioNuevoEsMenor() {
        assertTrue(Alerta.esBajadaDePrecio(new BigDecimal("50.00"), new BigDecimal("49.99")));
        assertFalse(Alerta.esBajadaDePrecio(new BigDecimal("50.00"), new BigDecimal("50")));
        assertFalse(Alerta.esBajadaDePrecio(new BigDecimal("50.00"), new BigDecimal("60.00")));
    }

    @Test
    @DisplayName("HU529-CP02: no se crea una alerta de bajada de precio si el precio no baja")
    void noHayAlertaDeBajadaSiElPrecioNoBaja() {
        BigDecimal cincuenta = new BigDecimal("50.00");
        BigDecimal sesenta = new BigDecimal("60.00");

        assertThrows(AlertaInvalidaException.class,
                () -> Alerta.bajadaDePrecio("c-1", "p-1", "Teclado", cincuenta, cincuenta, AHORA));
        assertThrows(AlertaInvalidaException.class,
                () -> Alerta.bajadaDePrecio("c-1", "p-1", "Teclado", cincuenta, sesenta, AHORA));
        assertThrows(AlertaInvalidaException.class,
                () -> Alerta.bajadaDePrecio("c-1", "p-1", "Teclado", null, cincuenta, AHORA));
    }

    @Test
    @DisplayName("HU529-CP03: el producto vuelve a estar disponible solo cuando pasa de no poder comprarse a poder comprarse")
    void vuelveAEstarDisponibleSoloAlPasarAComprable() {
        assertTrue(Alerta.vuelveAEstarDisponible(false, true));
        assertFalse(Alerta.vuelveAEstarDisponible(true, true));
        assertFalse(Alerta.vuelveAEstarDisponible(false, false));
        assertFalse(Alerta.vuelveAEstarDisponible(true, false));
    }

    @Test
    @DisplayName("HU529-CP03: una alerta de disponibilidad guarda el producto y su nombre, sin precios")
    void unaAlertaDeDisponibilidadNoLlevaPrecios() {
        Alerta alerta = Alerta.disponibilidad("c-1", "p-1", "Teclado", AHORA);

        assertAll(
                () -> assertEquals(TipoAlerta.DISPONIBILIDAD, alerta.getTipo()),
                () -> assertEquals("p-1", alerta.getProductoId()),
                () -> assertFalse(alerta.isLeida()),
                () -> assertEquals("Teclado", alerta.getDetalle().nombreProducto()),
                () -> assertNull(alerta.getDetalle().precioAnterior()),
                () -> assertNull(alerta.getDetalle().precioNuevo()));
    }

    @Test
    @DisplayName("HU529-CP04: una alerta sin cliente, sin producto, sin tipo o sin fecha no es válida")
    void unaAlertaSinSusDatosObligatoriosNoEsValida() {
        DetalleAlerta detalle = new DetalleAlerta("Teclado", null, null);

        assertThrows(AlertaInvalidaException.class,
                () -> new Alerta(null, " ", "p-1", TipoAlerta.DISPONIBILIDAD, AHORA, false, detalle));
        assertThrows(AlertaInvalidaException.class,
                () -> new Alerta(null, "c-1", null, TipoAlerta.DISPONIBILIDAD, AHORA, false, detalle));
        assertThrows(AlertaInvalidaException.class,
                () -> new Alerta(null, "c-1", "p-1", null, AHORA, false, detalle));
        assertThrows(AlertaInvalidaException.class,
                () -> new Alerta(null, "c-1", "p-1", TipoAlerta.DISPONIBILIDAD, null, false, detalle));
    }

    @Test
    @DisplayName("HU529-CP04: una alerta guardada conserva su id y si está leída, y admite el tipo de producto nuevo (AB#530)")
    void unaAlertaGuardadaConservaSusDatos() {
        Alerta alerta = new Alerta("a-1", "c-1", "p-1", TipoAlerta.PRODUCTO_NUEVO, AHORA, true, null);

        assertEquals("a-1", alerta.getId());
        assertTrue(alerta.isLeida());
        assertNull(alerta.getDetalle().nombreProducto());
    }
}
