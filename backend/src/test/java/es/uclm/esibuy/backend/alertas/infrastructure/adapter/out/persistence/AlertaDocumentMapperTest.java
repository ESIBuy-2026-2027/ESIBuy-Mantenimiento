package es.uclm.esibuy.backend.alertas.infrastructure.adapter.out.persistence;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Instant;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import es.uclm.esibuy.backend.alertas.domain.model.Alerta;
import es.uclm.esibuy.backend.alertas.domain.model.DetalleAlerta;
import es.uclm.esibuy.backend.alertas.domain.model.TipoAlerta;

// Sin Spring: conversión entre Alerta y su documento de MongoDB (AB#529).
// PENDIENTE DE CONFIRMAR CON EL EQUIPO: caso HU529-CP14, propuesto.
class AlertaDocumentMapperTest {

    private static final Instant AHORA = Instant.parse("2026-11-26T10:00:00Z");

    private final AlertaDocumentMapper mapper = new AlertaDocumentMapper();

    @Test
    @DisplayName("HU529-CP14: una alerta de bajada de precio pasa a documento y vuelve sin perder ningún dato")
    void convierteUnaBajadaDePrecioSinPerderDatos() {
        Alerta alerta = new Alerta("a-1", "c-1", "p-1", TipoAlerta.BAJADA_PRECIO, AHORA, true,
                new DetalleAlerta("Teclado", "https://img.test/p.png", new BigDecimal("50.00"), new BigDecimal("40.00")));

        AlertaDocument documento = mapper.aDocumento(alerta);
        Alerta leida = mapper.aDominio(documento);

        assertEquals("BAJADA_PRECIO", documento.getTipo());
        assertAll(
                () -> assertEquals("a-1", leida.getId()),
                () -> assertEquals("c-1", leida.getClienteId()),
                () -> assertEquals("p-1", leida.getProductoId()),
                () -> assertEquals(TipoAlerta.BAJADA_PRECIO, leida.getTipo()),
                () -> assertEquals(AHORA, leida.getFecha()),
                () -> assertTrue(leida.isLeida()),
                () -> assertEquals(alerta.getDetalle(), leida.getDetalle()));
    }

    @Test
    @DisplayName("HU529-CP14: una alerta de disponibilidad nueva pasa a documento sin id, no leída y sin precios")
    void convierteUnaAlertaDeDisponibilidadNueva() {
        AlertaDocument documento = mapper.aDocumento(Alerta.disponibilidad("c-1", "p-1", "Teclado", "https://img.test/p.png", AHORA));

        assertNull(documento.getId());
        assertEquals("DISPONIBILIDAD", documento.getTipo());
        assertFalse(documento.isLeida());
        assertEquals("Teclado", documento.getNombreProducto());
        assertEquals("https://img.test/p.png", documento.getImagenProducto());
        assertNull(documento.getPrecioAnterior());
        assertNull(documento.getPrecioNuevo());
    }
}
